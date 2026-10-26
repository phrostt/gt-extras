package com.gtextras.gt.mana;

import com.gtextras.GTExtras;

import vazkii.botania.api.mana.ManaReceiver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class AppbotLink {

    private static Class<?> safeMana;
    private static boolean failed;

    private AppbotLink() {}

    public static ManaReceiver wrap(ManaContainer container) {
        if (safeMana == null && !failed) {
            try {
                safeMana = Class.forName("appbot.ae2.SafeMana");
            } catch (Throwable t) {
                failed = true;
                GTExtras.LOGGER.warn("[{}] Applied Botanics mana bus support unavailable: {}", GTExtras.MOD_ID, t.toString());
            }
        }
        if (safeMana == null) return container;
        InvocationHandler handler = (proxy, method, args) -> handle(container, proxy, method, args);
        return (ManaReceiver) Proxy.newProxyInstance(AppbotLink.class.getClassLoader(),
                new Class<?>[] { ManaReceiver.class, safeMana }, handler);
    }

    private static Object handle(ManaContainer c, Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "insert": {
                int wanted = ((Number) args[0]).intValue();
                int accepted = Math.max(0, Math.min(wanted, c.getSpace()));
                if (accepted > 0 && "MODULATE".equals(String.valueOf(args[1]))) {
                    c.setStored(c.getStored() + accepted);
                }
                return accepted;
            }
            case "extract": {
                int wanted = ((Number) args[0]).intValue();
                int taken = Math.max(0, Math.min(wanted, c.getStored()));
                if (taken > 0 && "MODULATE".equals(String.valueOf(args[1]))) {
                    c.setStored(c.getStored() - taken);
                }
                return taken;
            }
            case "getManaReceiverLevel":
                return c.getManaReceiverLevel();
            case "getManaReceiverPos":
                return c.getManaReceiverPos();
            case "getCurrentMana":
                return c.getCurrentMana();
            case "isFull":
                return c.isFull();
            case "receiveMana":
                c.receiveMana(((Number) args[0]).intValue());
                return null;
            case "canReceiveManaFromBursts":
                return c.canReceiveManaFromBursts();
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            case "toString":
                return "GTExtrasManaHatch";
            default:
                if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
                return null;
        }
    }
}
