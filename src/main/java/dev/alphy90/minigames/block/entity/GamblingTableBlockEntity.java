package dev.alphy90.minigames.block.entity;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.GamblingStoolBlock;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GamblingTableBlockEntity extends BlockEntity{
    private boolean hasCards = false;
    private int checkCooldown = 0;
    private int pileCount = 8;

    private dev.alphy90.minigames.game.TavernCard playerCard = null;
    private dev.alphy90.minigames.game.TavernCard gamblerCard = null;

    public dev.alphy90.minigames.game.TavernCard getPlayerCard() { return playerCard; }
    public dev.alphy90.minigames.game.TavernCard getGamblerCard() { return gamblerCard; }

    public void playPlayerCard(dev.alphy90.minigames.game.TavernCard card, ServerPlayerEntity player){
        this.playerCard = card;
        this.gamblerCard = dev.alphy90.minigames.game.CardDeck.randomCard();

        if(this.world != null){
            this.world.playSound(null, this.pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0.8F, 1.2F);
            this.markDirty();
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public GamblingTableBlockEntity(BlockPos pos, BlockState state){
        super(MiniGames.GAMBLING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GamblingTableBlockEntity be){
        if(world.isClient()) return;

        if(be.checkCooldown-- <= 0){
            be.checkCooldown = 10;
            boolean gamblerSeated = be.checkCardinalsForSeatedGambler(world, pos);
            if(gamblerSeated != be.hasCards){
                be.hasCards = gamblerSeated;
                if(gamblerSeated && be.pileCount <= 0){
                    be.pileCount = 8;
                }
                be.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        }
    }

    private boolean checkCardinalsForSeatedGambler(World world, BlockPos tablePos){
        for(Direction dir : Direction.Type.HORIZONTAL){
            BlockPos stoolPos = tablePos.offset(dir);
            if(world.getBlockState(stoolPos).getBlock() instanceof GamblingStoolBlock){
                Box box = new Box(stoolPos).expand(0.2);
                for (SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, box, SeatEntity::hasPassengers)){
                    if(seat.getFirstPassenger() instanceof GamblerEntity){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ActionResult onPlayerInteract(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.util.hit.BlockHitResult hit){
        if(this.world == null || !(this.world instanceof net.minecraft.server.world.ServerWorld serverWorld)) return ActionResult.PASS;

        boolean conditionsMet = areGameConditionsMet(serverWorld, player);

        //all met
        if(conditionsMet){
            if(this.pileCount <= 0){
                this.pileCount = 8;
                this.markDirty();
                this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
            }
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new dev.alphy90.minigames.network.TablePlayingPayload(true));
            return ActionResult.SUCCESS;
        }

        if(this.hasCards && this.pileCount > 0){
            this.pileCount--;
            player.giveItemStack(new net.minecraft.item.ItemStack(MiniGames.TAVERN_CARD));
            this.world.playSound(null, this.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.6F, 1.2F);
            this.markDirty();
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public boolean areGameConditionsMet(net.minecraft.server.world.ServerWorld serverWorld, net.minecraft.server.network.ServerPlayerEntity player){
        if(!player.hasVehicle() || !(player.getVehicle() instanceof dev.alphy90.minigames.entity.SeatEntity)){
            return false;
        }

        BlockPos playerStoolPos = player.getVehicle().getBlockPos();

        Direction tableDir = null;
        for(Direction dir : Direction.Type.HORIZONTAL){
            if(playerStoolPos.offset(dir).equals(this.pos)){
                tableDir = dir;
                break;
            }
        }
        if(tableDir == null) return false;

        BlockPos oppositeStoolPos = this.pos.offset(tableDir);
        Box box = new Box(oppositeStoolPos).expand(0.2);
        boolean gamblerPresentOpposite = false;
        for(dev.alphy90.minigames.entity.SeatEntity seat : serverWorld.getEntitiesByClass(dev.alphy90.minigames.entity.SeatEntity.class, box, dev.alphy90.minigames.entity.SeatEntity::hasPassengers)){
            if(seat.getFirstPassenger() instanceof dev.alphy90.minigames.entity.GamblerEntity){
                gamblerPresentOpposite = true;
                break;
            }
        }
        if(!gamblerPresentOpposite) return false;

        net.minecraft.village.raid.Raid raid = serverWorld.getRaidAt(this.pos);
        if(raid != null && raid.isActive()){
            return false;
        }
        return true;
    }

    public int getPileCount(){
        return pileCount;
    }

    public boolean isHasCards(){
        return hasCards && pileCount > 0;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries){
        super.writeNbt(nbt, registries);
        nbt.putBoolean("HasCards", hasCards);
        nbt.putInt("PileCount", pileCount);
        if(playerCard != null){
            nbt.putInt("PlayerCardSuit", playerCard.suit().ordinal());
            nbt.putInt("PlayerCardVal", playerCard.value());
        }
        if(gamblerCard != null){
            nbt.putInt("GamblerCardSuit", gamblerCard.suit().ordinal());
            nbt.putInt("GamblerCardVal", gamblerCard.value());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries){
        super.readNbt(nbt, registries);
        hasCards = nbt.getBoolean("HasCards");
        pileCount = nbt.contains("PileCount") ? nbt.getInt("PileCount") : 8;
        if(nbt.contains("PlayerCardSuit")){
            playerCard = new dev.alphy90.minigames.game.TavernCard(
                    dev.alphy90.minigames.game.CardSuit.values()[nbt.getInt("PlayerCardSuit")],
                    nbt.getInt("PlayerCardVal")
            );
        } else {
            playerCard = null;
        }

        if(nbt.contains("GamblerCardSuit")){
            gamblerCard = new dev.alphy90.minigames.game.TavernCard(
                    dev.alphy90.minigames.game.CardSuit.values()[nbt.getInt("GamblerCardSuit")],
                    nbt.getInt("GamblerCardVal")
            );
        } else {
            gamblerCard = null;
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket(){
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries){
        return createNbt(registries);
    }
}