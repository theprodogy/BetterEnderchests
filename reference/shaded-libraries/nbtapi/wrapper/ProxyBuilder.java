/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.NBTHandler;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.wrapper.Casing;
import com.fernsehheft.enderchest.nbtapi.wrapper.DefaultMethodInvoker;
import com.fernsehheft.enderchest.nbtapi.wrapper.NBTProxy;
import com.fernsehheft.enderchest.nbtapi.wrapper.NBTTarget;
import com.fernsehheft.enderchest.nbtapi.wrapper.ProxiedList;
import com.fernsehheft.enderchest.nbtapi.wrapper.ProxyList;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ProxyBuilder<T extends NBTProxy>
implements InvocationHandler {
    private static final Map<Method, Function<Arguments, Object>> METHOD_CACHE = new ConcurrentHashMap<Method, Function<Arguments, Object>>();
    private final Class<T> target;
    private final ReadWriteNBT nbt;
    private boolean readOnly;

    public ProxyBuilder(ReadWriteNBT nbt, Class<T> target) {
        if (!target.isInterface()) {
            throw new NbtApiException("A proxy can only be built from an interface! Check the wiki for examples.");
        }
        this.target = target;
        this.nbt = nbt;
    }

    public ProxyBuilder<T> readOnly() {
        this.readOnly = true;
        return this;
    }

    public T build() {
        NBTProxy inst = (NBTProxy)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{this.target}, this);
        inst.init();
        return (T)inst;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        METHOD_CACHE.computeIfAbsent(method, m -> ProxyBuilder.createFunction((NBTProxy)proxy, m));
        return METHOD_CACHE.get(method).apply(new Arguments(this.target, (NBTProxy)proxy, this.readOnly, this.nbt, args));
    }

    private static Function<Arguments, Object> createFunction(NBTProxy proxy, Method method) {
        if ("toString".equals(method.getName()) && method.getParameterCount() == 0 && method.getReturnType() == String.class) {
            return arguments -> arguments.nbt.toString();
        }
        if (method.isDefault()) {
            return arguments -> DefaultMethodInvoker.invokeDefault(arguments.target, arguments.proxy, method, arguments.args);
        }
        NBTTarget.Type action = ProxyBuilder.getAction(method);
        if (action == NBTTarget.Type.SET) {
            String fieldName = ProxyBuilder.getNBTName(proxy.getCasing(), method);
            return arguments -> {
                if (arguments.readOnly) {
                    throw new NbtApiException("Tried calling a set method on a read only object.");
                }
                return ProxyBuilder.setNBT(arguments.nbt, arguments.proxy, fieldName, arguments.args[0]);
            };
        }
        if (action == NBTTarget.Type.GET) {
            Class parameterType;
            Class<?> retType = method.getReturnType();
            String fieldName = ProxyBuilder.getNBTName(proxy.getCasing(), method);
            if (retType.isInterface() && NBTProxy.class.isAssignableFrom(retType)) {
                return arguments -> {
                    if (arguments.nbt.hasTag(fieldName) && arguments.nbt.getType(fieldName) != NBTType.NBTTagCompound) {
                        throw new NbtApiException("Tried getting a '" + retType + "' proxy from the field '" + fieldName + "', but it's not a TagCompound!");
                    }
                    return new ProxyBuilder(arguments.nbt.getOrCreateCompound(fieldName), retType).build();
                };
            }
            if (retType == ProxyList.class && (parameterType = (Class)((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0]) != null && parameterType.isInterface() && NBTProxy.class.isAssignableFrom(parameterType)) {
                return arguments -> new ProxiedList(arguments.nbt.getCompoundList(fieldName), parameterType);
            }
            NBTHandler<?> handler = proxy.getHandler(retType);
            if (handler != null) {
                return arguments -> handler.get(arguments.nbt, fieldName);
            }
            return arguments -> arguments.nbt.getOrNull(fieldName, retType);
        }
        if (action == NBTTarget.Type.HAS) {
            String fieldName = ProxyBuilder.getNBTName(proxy.getCasing(), method);
            return arguments -> arguments.nbt.hasTag(fieldName);
        }
        throw new IllegalArgumentException("The method '" + method.getName() + "' in '" + method.getDeclaringClass().getName() + "' can not be handled by the NBT-API. Please check the Wiki for examples!");
    }

    private static NBTTarget.Type getAction(Method method) {
        NBTTarget target = method.getAnnotation(NBTTarget.class);
        if (target != null) {
            if (target.type() == NBTTarget.Type.HAS && method.getParameterCount() == 0 && method.getReturnType() == Boolean.TYPE) {
                return NBTTarget.Type.HAS;
            }
            if (target.type() == NBTTarget.Type.GET && method.getParameterCount() == 0) {
                return NBTTarget.Type.GET;
            }
            if (target.type() == NBTTarget.Type.SET && method.getParameterCount() == 1) {
                return NBTTarget.Type.SET;
            }
        }
        if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
            return NBTTarget.Type.SET;
        }
        if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
            return NBTTarget.Type.GET;
        }
        if (method.getName().startsWith("has") && method.getParameterCount() == 0 && method.getReturnType() == Boolean.TYPE) {
            return NBTTarget.Type.HAS;
        }
        return null;
    }

    private static String getNBTName(Casing casing, Method method) {
        NBTTarget target = method.getAnnotation(NBTTarget.class);
        if (target != null) {
            return target.value();
        }
        return casing.convertString(method.getName().substring(3));
    }

    private static Object setNBT(ReadWriteNBT nbt, NBTProxy proxy, String key, Object value) {
        if (value == null) {
            nbt.removeKey(key);
        } else if (value instanceof Boolean) {
            nbt.setBoolean(key, (Boolean)value);
        } else if (value instanceof Byte) {
            nbt.setByte(key, (Byte)value);
        } else if (value instanceof Short) {
            nbt.setShort(key, (Short)value);
        } else if (value instanceof Integer) {
            nbt.setInteger(key, (Integer)value);
        } else if (value instanceof Long) {
            nbt.setLong(key, (Long)value);
        } else if (value instanceof Float) {
            nbt.setFloat(key, (Float)value);
        } else if (value instanceof Double) {
            nbt.setDouble(key, (Double)value);
        } else if (value instanceof byte[]) {
            nbt.setByteArray(key, (byte[])value);
        } else if (value instanceof int[]) {
            nbt.setIntArray(key, (int[])value);
        } else if (value instanceof long[]) {
            nbt.setLongArray(key, (long[])value);
        } else if (value instanceof String) {
            nbt.setString(key, (String)value);
        } else if (value instanceof UUID) {
            nbt.setUUID(key, (UUID)value);
        } else if (value.getClass().isEnum()) {
            nbt.setEnum(key, (Enum)value);
        } else {
            NBTHandler<?> handler = proxy.getHandler(value.getClass());
            if (handler != null) {
                handler.set(nbt, key, value);
            } else {
                for (NBTHandler<Object> nbth : proxy.getHandlers()) {
                    if (!nbth.fuzzyMatch(value)) continue;
                    nbth.set(nbt, key, value);
                    return null;
                }
                throw new IllegalArgumentException("Tried setting an object of type '" + value.getClass().getName() + "'. This is not a supported NBT value. Please check the Wiki for examples!");
            }
        }
        return null;
    }

    private static class Arguments {
        Class<?> target;
        NBTProxy proxy;
        ReadWriteNBT nbt;
        Object[] args;
        boolean readOnly;

        public Arguments(Class<?> target, NBTProxy proxy, boolean readOnly, ReadWriteNBT nbt, Object[] args) {
            this.target = target;
            this.proxy = proxy;
            this.nbt = nbt;
            this.args = args;
            this.readOnly = readOnly;
        }
    }
}

