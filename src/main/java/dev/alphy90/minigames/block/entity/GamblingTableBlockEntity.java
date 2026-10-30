package dev.alphy90.minigames.block.entity;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.GamblingStoolBlock;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;

import dev.alphy90.minigames.game.TavernCard;
import dev.alphy90.minigames.network.TablePlayingPayload;
import dev.alphy90.minigames.network.TableScorePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GamblingTableBlockEntity extends BlockEntity {
    private boolean hasCards = false;
    private int checkCooldown = 0;
    private int pileCount = 8;

    private final java.util.List<dev.alphy90.minigames.game.TavernCard> playerHand = new java.util.ArrayList<>();
    private final java.util.List<dev.alphy90.minigames.game.TavernCard> gamblerHand = new java.util.ArrayList<>();
    private int matchDifficulty = 0;
    private int matchBet = 0;
    private int totalCards = 0;
    private int playerScore = 0;
    private int gamblerScore = 0;
    private int clearCardsCooldown = 0;
    private int currentRound = 1;
    private int gamblerPlayDelay = 0;
    private java.util.UUID activePlayerUuid = null;

    public java.util.List<dev.alphy90.minigames.game.TavernCard> getPlayerHand() {return playerHand;}
    public java.util.List<dev.alphy90.minigames.game.TavernCard> getGamblerHand() {return gamblerHand;}

    private dev.alphy90.minigames.game.TavernCard playerCard = null;
    private dev.alphy90.minigames.game.TavernCard gamblerCard = null;

    public dev.alphy90.minigames.game.TavernCard getPlayerCard() {
        return playerCard;
    }

    public dev.alphy90.minigames.game.TavernCard getGamblerCard() {
        return gamblerCard;
    }

    private dev.alphy90.minigames.game.TavernCard pickGamblerCard(dev.alphy90.minigames.game.TavernCard playerCard, int difficulty) {
        if (gamblerHand.isEmpty()) {
            return dev.alphy90.minigames.game.CardDeck.randomCard();
        }
        if (difficulty == 1) {
            int idx = this.world != null ? this.world.random.nextInt(gamblerHand.size()) : 0;
            return gamblerHand.remove(idx);
        }
        if (difficulty == 0 && this.world != null && this.world.random.nextBoolean()) {
            int idx = this.world.random.nextInt(gamblerHand.size());
            return gamblerHand.remove(idx);
        }
        dev.alphy90.minigames.game.TavernCard bestWin = null;
        dev.alphy90.minigames.game.TavernCard lowest = gamblerHand.get(0);
        for (dev.alphy90.minigames.game.TavernCard c : gamblerHand) {
            if (c.compareWith(playerCard) == dev.alphy90.minigames.game.TavernCard.RoundResult.WIN) {
                if (bestWin == null || c.value() < bestWin.value()) {
                    bestWin = c;
                }
            }
            if (c.value() < lowest.value()) {
                lowest = c;
            }
        }
        dev.alphy90.minigames.game.TavernCard chosen = (bestWin != null) ? bestWin : lowest;
        gamblerHand.remove(chosen);
        return chosen;
    }

    public void playPlayerCard(dev.alphy90.minigames.game.TavernCard card, ServerPlayerEntity player) {
        if (!this.gameActive || this.clearCardsCooldown > 0 || this.gamblerPlayDelay > 0 || this.playerCard != null)
            return;

        this.playerCard = card;
        this.gamblerCard = null;
        this.gamblerPlayDelay = 20;
        this.markDirty();

        if (this.world != null) {
            this.world.playSound(null, this.pos, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.9f, 1.4f);
            this.world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
        }
    }

    public void startGame(ServerPlayerEntity player, int bet, int cards, int difficulty) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return;
        if (!areGameConditionsMet(serverWorld, player) || this.gameActive)
            return;

        int emeraldsInInventory = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.EMERALD)) {
                emeraldsInInventory += stack.getCount();
            }
        }

        if (emeraldsInInventory < bet) {
            player.sendMessage(Text.literal("§cYou don't have enough emeralds to bet " + bet + "!"), true);
            player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            ServerPlayNetworking.send(player, new TablePlayingPayload(false, 0, 0));
            return;
        }

        int remainingToDeduct = bet;
        for (int i = 0; i < player.getInventory().size() && remainingToDeduct > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.EMERALD)) {
                int take = Math.min(stack.getCount(), remainingToDeduct);
                stack.decrement(take);
                remainingToDeduct -= take;
            }
        }

        this.gameActive = true;
        this.matchBet = bet;
        this.totalCards = cards;
        this.matchDifficulty = difficulty;
        this.activePlayerUuid = player.getUuid();
        this.playerScore = 0;
        this.gamblerScore = 0;
        this.clearCardsCooldown = 0;
        this.playerCard = null;
        this.gamblerCard = null;

        this.playerHand.clear();
        this.gamblerHand.clear();
        this.playerHand.addAll(dev.alphy90.minigames.game.CardDeck.dealHand(cards));
        this.gamblerHand.addAll(dev.alphy90.minigames.game.CardDeck.dealHand(cards));

        this.markDirty();
        this.world.playSound(null, this.pos, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.BLOCKS, 0.8F, 1.2F);
        this.currentRound = 1;
        this.gamblerPlayDelay = 0;
        player.sendMessage(Text.literal("\u00A7a\u00A7lGood Luck! \u00A77Watch your strategy carefully."), true);
        ServerPlayNetworking.send(player, new TablePlayingPayload(true, cards, bet));
    }

    public GamblingTableBlockEntity(BlockPos pos, BlockState state) {
        super(MiniGames.GAMBLING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GamblingTableBlockEntity be) {
        if (world.isClient())
            return;

        if(be.gamblerPlayDelay > 0){
            be.gamblerPlayDelay--;
            if(be.gamblerPlayDelay == 0){
                be.gamblerCard = be.pickGamblerCard(be.playerCard, be.matchDifficulty);
                world.playSound(null, pos, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.BLOCKS, 0.9f, 1.0f);
                be.markDirty();
                world.updateListeners(pos, state, state, 3);

                ServerPlayerEntity player = (be.activePlayerUuid != null && world instanceof ServerWorld sw && sw.getPlayerByUuid(be.activePlayerUuid) instanceof ServerPlayerEntity spe) ? spe : null;
                boolean isTie = (be.playerCard.suit() == be.gamblerCard.suit() && be.playerCard.value() == be.gamblerCard.value());

                if(isTie){
                    be.clearCardsCooldown = 65;
                    dev.alphy90.minigames.game.TavernCard replacement = dev.alphy90.minigames.game.CardDeck.randomCard();
                    be.gamblerHand.add(dev.alphy90.minigames.game.CardDeck.randomCard());
                    if(player != null){
                        player.sendMessage(Text.literal("\u00A7e\u00A7lTie! \u00A77Cards discarded, drawing a new card..."), true);
                        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
                        ServerPlayNetworking.send(player, new TableScorePayload(be.playerScore, be.gamblerScore, 0, be.currentRound, replacement.suit().ordinal(), replacement.value()));
                    }
                } else {
                    TavernCard.RoundResult res = be.playerCard.compareWith(be.gamblerCard);
                    be.currentRound++;
                    be.clearCardsCooldown = 65;
                    if(res == TavernCard.RoundResult.WIN){
                        be.playerScore++;
                        if(player != null){
                            player.sendMessage(Text.literal("\u00A7a\u00A7lRound Won! \u00A77Your card defeated the Gambler's."), true);
                            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                            ServerPlayNetworking.send(player, new TableScorePayload(be.playerScore, be.gamblerScore, 1, be.currentRound, -1, -1));
                        }
                    } else {
                        be.gamblerScore++;
                        if(player != null){
                            player.sendMessage(Text.literal("\u00A7c\u00A7lRound Lost! \u00A77The Gambler took the round."), true);
                            player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 0.9f);
                            ServerPlayNetworking.send(player, new TableScorePayload(be.playerScore, be.gamblerScore, -1, be.currentRound, -1, -1));
                        }
                    }
                }
            }
        }

        if (be.clearCardsCooldown > 0) {
            be.clearCardsCooldown--;
            if (be.clearCardsCooldown == 0) {
                int scoreToWin = (be.totalCards / 2) + 1;
                boolean matchFinished = be.gamblerHand.isEmpty() || be.playerScore >= scoreToWin || be.gamblerScore >= scoreToWin;
                if (matchFinished) {
                    ServerPlayerEntity player = (be.activePlayerUuid != null && world instanceof ServerWorld sw && sw.getPlayerByUuid(be.activePlayerUuid) instanceof ServerPlayerEntity spe)
                            ? spe : null;
                    if (player != null) {
                        if (be.playerScore > be.gamblerScore) {
                            int pot = 2 * be.matchBet;
                            player.getInventory().offerOrDrop(new ItemStack(Items.EMERALD, pot));
                            player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                            player.sendMessage(Text.literal("\u00A76\u00A7lVICTORY! \u00A7aYou won " + pot + " Emeralds!"), false);
                        } else if (be.gamblerScore > be.playerScore) {
                            player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.9F);
                            player.sendMessage(Text.literal("\u00A7c\u00A7lDEFEAT! \u00A77The Gambler took the pot."), false);
                        } else {
                            player.getInventory().offerOrDrop(new ItemStack(Items.EMERALD, be.matchBet));
                            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                            player.sendMessage(Text.literal("\u00A7e\u00A7lMATCH TIED! \u00A77Your bet was returned."), false);
                        }
                        ServerPlayNetworking.send(player, new TablePlayingPayload(false, 0, 0));
                    }
                    be.gameActive = false;
                    be.playerCard = null;
                    be.gamblerCard = null;
                    be.gamblerHand.clear();
                    be.playerHand.clear();
                    be.markDirty();
                    world.updateListeners(pos, state, state, 3);
                } else {
                    be.playerCard = null;
                    be.gamblerCard = null;
                    be.markDirty();
                    world.updateListeners(pos, state, state, 3);
                }
            }
        }

        if (be.checkCooldown-- <= 0) {
            be.checkCooldown = 10;
            boolean gamblerSeated = be.checkCardinalsForSeatedGambler(world, pos);
            if (gamblerSeated != be.hasCards) {
                be.hasCards = gamblerSeated;
                if (gamblerSeated && be.pileCount <= 0) {
                    be.pileCount = 8;
                }
                be.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        }
    }

    private boolean checkCardinalsForSeatedGambler(World world, BlockPos tablePos) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos stoolPos = tablePos.offset(dir);
            if (world.getBlockState(stoolPos).getBlock() instanceof GamblingStoolBlock) {
                Box box = new Box(stoolPos).expand(0.2);
                for (SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, box, SeatEntity::hasPassengers)) {
                    if (seat.getFirstPassenger() instanceof GamblerEntity) {
                        Direction playerFacing = dir.getOpposite();
                        if (this.getCachedState()
                                .get(dev.alphy90.minigames.block.GamblingTableBlock.FACING) != playerFacing) {
                            world.setBlockState(tablePos, this.getCachedState()
                                    .with(dev.alphy90.minigames.block.GamblingTableBlock.FACING, playerFacing));
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ActionResult onPlayerInteract(net.minecraft.server.network.ServerPlayerEntity player,
            net.minecraft.util.hit.BlockHitResult hit) {
        if (this.world == null || !(this.world instanceof net.minecraft.server.world.ServerWorld serverWorld))
            return ActionResult.PASS;

        boolean conditionsMet = areGameConditionsMet(serverWorld, player);

        // all met
        if (conditionsMet) {
            if (this.pileCount <= 0) {
                this.pileCount = 8;
                this.markDirty();
                this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
            }
            if (!this.gameActive) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                        new dev.alphy90.minigames.network.OpenTableScreenPayload(this.pos));
            }
            return ActionResult.SUCCESS;
        }

        if (this.hasCards && this.pileCount > 0 && hit.getPos().y > this.pos.getY() + 1.0) {
            this.pileCount--;
            player.giveItemStack(new net.minecraft.item.ItemStack(MiniGames.TAVERN_CARD));
            this.world.playSound(null, this.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.6F, 1.2F);
            this.markDirty();
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public boolean areGameConditionsMet(net.minecraft.server.world.ServerWorld serverWorld,
            net.minecraft.server.network.ServerPlayerEntity player) {
        if (!player.hasVehicle() || !(player.getVehicle() instanceof dev.alphy90.minigames.entity.SeatEntity)) {
            return false;
        }

        BlockPos playerStoolPos = player.getVehicle().getBlockPos();

        Direction tableDir = null;
        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (playerStoolPos.offset(dir).equals(this.pos)) {
                tableDir = dir;
                break;
            }
        }
        if (tableDir == null)
            return false;

        BlockPos oppositeStoolPos = this.pos.offset(tableDir);
        Box box = new Box(oppositeStoolPos).expand(0.2);
        boolean gamblerPresentOpposite = false;
        for (dev.alphy90.minigames.entity.SeatEntity seat : serverWorld.getEntitiesByClass(
                dev.alphy90.minigames.entity.SeatEntity.class, box,
                dev.alphy90.minigames.entity.SeatEntity::hasPassengers)) {
            if (seat.getFirstPassenger() instanceof dev.alphy90.minigames.entity.GamblerEntity) {
                gamblerPresentOpposite = true;
                break;
            }
        }
        if (!gamblerPresentOpposite)
            return false;

        net.minecraft.village.raid.Raid raid = serverWorld.getRaidAt(this.pos);
        if (raid != null && raid.isActive()) {
            return false;
        }
        return true;
    }

    public int getPileCount() {
        return pileCount;
    }

    public boolean isHasCards() {
        return hasCards && pileCount > 0;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putBoolean("HasCards", hasCards);
        nbt.putInt("PileCount", pileCount);
        nbt.putInt("PlayerScore", playerScore);
        nbt.putInt("GamblerScore", gamblerScore);
        nbt.putInt("TotalCards", totalCards);
        nbt.putInt("MatchBet", matchBet);
        nbt.putInt("MatchDifficulty", matchDifficulty);
        nbt.putBoolean("GameActive", gameActive);
        if (playerCard != null) {
            nbt.putInt("PlayerCardSuit", playerCard.suit().ordinal());
            nbt.putInt("PlayerCardVal", playerCard.value());
        }
        if (gamblerCard != null) {
            nbt.putInt("GamblerCardSuit", gamblerCard.suit().ordinal());
            nbt.putInt("GamblerCardVal", gamblerCard.value());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        hasCards = nbt.getBoolean("HasCards");
        pileCount = nbt.contains("PileCount") ? nbt.getInt("PileCount") : 8;
        playerScore = nbt.getInt("PlayerScore");
        gamblerScore = nbt.getInt("GamblerScore");
        totalCards = nbt.getInt("TotalCards");
        matchBet = nbt.getInt("MatchBet");
        matchDifficulty = nbt.getInt("MatchDifficulty");
        gameActive = nbt.getBoolean("GameActive");
        if (nbt.contains("PlayerCardSuit")) {
            playerCard = new dev.alphy90.minigames.game.TavernCard(
                    dev.alphy90.minigames.game.CardSuit.values()[nbt.getInt("PlayerCardSuit")],
                    nbt.getInt("PlayerCardVal"));
        } else {
            playerCard = null;
        }

        if (nbt.contains("GamblerCardSuit")) {
            gamblerCard = new dev.alphy90.minigames.game.TavernCard(
                    dev.alphy90.minigames.game.CardSuit.values()[nbt.getInt("GamblerCardSuit")],
                    nbt.getInt("GamblerCardVal"));
        } else {
            gamblerCard = null;
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    private boolean gameActive = false;

    public boolean isGameActive() {
        return this.gameActive;
    }

    public void setGameActive(boolean active) {
        this.gameActive = active;
        this.markDirty();
    }
}