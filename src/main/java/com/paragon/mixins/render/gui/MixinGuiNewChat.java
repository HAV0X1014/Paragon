package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.event.render.gui.GetChatLineCountEvent;
import com.paragon.impl.event.render.gui.RenderChatEvent;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    public void hookDrawChat(int x, int y, int x2, int y2, int colour) {
        RenderChatEvent chatEvent = new RenderChatEvent(colour);
        Paragon.INSTANCE.getEventBus().post(chatEvent);

        if (!chatEvent.isCancelled()) {
            Gui.drawRect(x, y, x2, y2, chatEvent.getColour());
        }
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    public int hookSetChatLineOrd0(List<ChatLine> instance) {
        GetChatLineCountEvent event = new GetChatLineCountEvent(instance.size());
        Paragon.INSTANCE.getEventBus().post(event);

        return event.getSize();
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2))
    public int hookSetChatLineOrd2(List<ChatLine> instance) {
        GetChatLineCountEvent event = new GetChatLineCountEvent(instance.size());
        Paragon.INSTANCE.getEventBus().post(event);

        return event.getSize();
    }

}
