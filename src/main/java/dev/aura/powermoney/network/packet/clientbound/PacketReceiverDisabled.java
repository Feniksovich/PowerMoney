package dev.aura.powermoney.network.packet.clientbound;

import dev.aura.powermoney.client.gui.GuiPowerReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketReceiverDisabled implements IMessage {
  public PacketReceiverDisabled() {}

  @Override
  public void fromBytes(ByteBuf buf) {
    // Nothing
  }

  @Override
  public void toBytes(ByteBuf buf) {
    // Nothing
  }

  public static class Handler extends AbstractClientMessageHandler<PacketReceiverDisabled> {
    @Override
    public IMessage handleClientMessage(
        EntityPlayer player, PacketReceiverDisabled message, MessageContext ctx) {
      GuiPowerReceiver.receiverDisabled();

      return null;
    }
  }
}
