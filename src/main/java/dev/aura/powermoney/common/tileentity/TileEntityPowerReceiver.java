package dev.aura.powermoney.common.tileentity;

import dev.aura.powermoney.PowerMoneyBlocks;
import dev.aura.powermoney.common.capability.EnergyConsumer;
import dev.aura.powermoney.common.helper.WorldBlockPos;
import java.util.UUID;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityPowerReceiver extends TileEntity {
  public static final UUID UUID_NOBODY = new UUID(0, 0);
  public static final String NAME_NOBODY = "<nobody>";

  @Getter private String customName;

  @Getter private UUID owner = UUID_NOBODY;
  @Getter private String ownerName = NAME_NOBODY;

  @Getter private EnergyConsumer energyConsumer = new EnergyConsumer();

  private World createWorld;

  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return energyConsumer.hasCapability(capability, facing);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if (hasCapability(capability, facing)) return (T) energyConsumer;

    return super.getCapability(capability, facing);
  }

  public void setCustomName(String newCustomName) {
    customName = newCustomName;

    markDirty();
  }

  public boolean hasCustomName() {
    return customName != null && !customName.isEmpty();
  }

  @Override
  public ITextComponent getDisplayName() {
    return this.hasCustomName()
        ? new TextComponentString(customName)
        : new TextComponentTranslation("container.power_receiver");
  }

  public void setOwner(UUID newOwner, String newOwnerName) {
    setOwner(newOwner);

    ownerName = newOwnerName;
  }

  public void setOwner(UUID newOwner) {
    owner = newOwner;
    energyConsumer =
        new EnergyConsumer(owner, new WorldBlockPos((world == null) ? createWorld : world, pos));

    if (owner == null) {
      ownerName = null;
    } else {
      final String nameFromCache = UsernameCache.getLastKnownUsername(owner);
      if (nameFromCache != null) {
        ownerName = nameFromCache;
      }
    }

    markDirty();
  }

  @Override
  protected void setWorldCreate(World worldIn) {
    createWorld = worldIn;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    setOwner(compound.getUniqueId("Owner"));
    setCustomName(compound.getString("CustomName"));
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);

    compound.setUniqueId("Owner", getOwner());

    if (hasCustomName()) compound.setString("CustomName", getCustomName());
    else compound.removeTag("CustomName");

    return compound;
  }

  @Override
  public void handleUpdateTag(NBTTagCompound compound) {
    readFromNBT(compound);

    ownerName = compound.getString("OwnerName");
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    NBTTagCompound compound = new NBTTagCompound();

    compound.setString("OwnerName", ownerName);

    compound = writeToNBT(compound);

    return compound;
  }

  @Override
  public boolean shouldRefresh(
      World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
    // false means to keep the TE
    return newState.getBlock() != PowerMoneyBlocks.powerReceiver();
  }

  public static TileEntityPowerReceiver getTileEntityAt(World world, BlockPos pos) {
    final TileEntity tempTileEntity = world.getTileEntity(pos);

    if ((tempTileEntity == null) || !(tempTileEntity instanceof TileEntityPowerReceiver))
      return null;
    else return (TileEntityPowerReceiver) tempTileEntity;
  }
}
