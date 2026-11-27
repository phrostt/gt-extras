package com.gtextras.gt.source;

import com.gtextras.GTExtras;

import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ArsEngLink {

    private static Capability<?> capability;
    private static Class<?> tileType;
    private static boolean failed;

    private ArsEngLink() {}

    public static Capability<?> capability() {
        if (capability == null && !failed) {
            try {
                Class<?> caps = Class.forName("gripe._90.arseng.definition.ArsEngCapabilities");
                capability = (Capability<?>) caps.getField("SOURCE_TILE").get(null);
                tileType = Class.forName("gripe._90.arseng.block.entity.IAdvancedSourceTile");
            } catch (Throwable t) {
                failed = true;
                GTExtras.LOGGER.warn("[{}] Ars Energistique source bus support unavailable: {}", GTExtras.MOD_ID, t.toString());
            }
        }
        return capability;
    }

    public static Object wrap(SourceContainer container) {
        if (capability() == null) return null;
        InvocationHandler handler = (proxy, method, args) -> handle(container, proxy, method, args);
        return Proxy.newProxyInstance(ArsEngLink.class.getClassLoader(), new Class<?>[] { tileType }, handler);
    }

    private static Object handle(SourceContainer c, Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getSource":
                return c.getStored();
            case "getMaxSource":
                return c.getCapacity();
            case "getTransferRate":
                return c.maxPerTick();
            case "canAcceptSource":
                return c.getSpace() > 0;
            case "relayCanTakePower":
            case "sourcelinksCanProvidePower":
                return false;
            case "setMaxSource":
                return null;
            case "setSource":
                return c.getStored();
            case "addSource": {
                int give = Math.min(((Number) args[0]).intValue(), c.getSpace());
                if (give > 0) c.setStored(c.getStored() + give);
                return c.getStored();
            }
            case "removeSource": {
                int take = Math.min(((Number) args[0]).intValue(), c.getStored());
                if (take > 0) c.setStored(c.getStored() - take);
                return c.getStored();
            }
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            case "toString":
                return "GTExtrasSourceHatch";
            default:
                if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
                Class<?> type = method.getReturnType();
                if (type == boolean.class) return false;
                if (type == int.class) return 0;
                if (type == long.class) return 0L;
                if (type == float.class) return 0f;
                if (type == double.class) return 0d;
                return null;
        }
    }
}
