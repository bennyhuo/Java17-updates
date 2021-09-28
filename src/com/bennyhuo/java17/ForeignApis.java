package com.bennyhuo.java17;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.SymbolLookup;
import sun.misc.Unsafe;

import static jdk.incubator.foreign.CLinker.C_CHAR;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.CLinker.C_SHORT;
import static jdk.incubator.foreign.ResourceScope.newImplicitScope;

/**
 * Created by benny.
 */
public class ForeignApis {
    public static void main(String[] args) {
//        useUnsafe();
//        useNewMemoryApi();
        callNativeFunctions();
    }

    public static void onEach(int element) {
        System.out.println("onEach: " + element);
    }

    public static void callNativeFunctions() {
        try {
            System.loadLibrary("libsimple");
            SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
            MemoryAddress getCLangVersion = loaderLookup.lookup("GetCLangVersion").get();
            MethodHandle getClangVersionHandle = CLinker.getInstance().downcallHandle(getCLangVersion,
                    MethodType.methodType(int.class),
                    FunctionDescriptor.of(C_INT));

            System.out.println(getClangVersionHandle.invoke());

            MemoryLayout personLayout = MemoryLayout.structLayout(
                    C_LONG_LONG.withName("id"),
                    MemoryLayout.sequenceLayout(10, C_CHAR).withName("name"),
                    MemoryLayout.paddingLayout(16),
                    C_INT.withName("age"));

            MemorySegment person = MemorySegment.allocateNative(personLayout, newImplicitScope());
            VarHandle idHandle = personLayout.varHandle(long.class, MemoryLayout.PathElement.groupElement("id"));
            idHandle.set(person, 1000000);

            VarHandle nameHandle = personLayout.varHandle(
                    byte.class,
                    MemoryLayout.PathElement.groupElement("name"),
                    MemoryLayout.PathElement.sequenceElement()
            );
            byte[] bytes = "bennyhuo".getBytes();
            for (int i = 0; i < bytes.length; i++) {
                nameHandle.set(person, i, bytes[i]);
            }
            nameHandle.set(person, bytes.length, (byte) 0);

            person.asSlice(personLayout.byteOffset(MemoryLayout.PathElement.groupElement("name"))).copyFrom(CLinker.toCString("bennyhuo", newImplicitScope()));

            var ageHandle = personLayout.varHandle(int.class, MemoryLayout.PathElement.groupElement("age"));
            ageHandle.set(person, 30);

            MemoryAddress dumpPerson = loaderLookup.lookup("DumpPerson").get();
            MethodHandle dumpPersonHandle = CLinker.getInstance().downcallHandle(
                    dumpPerson,
                    MethodType.methodType(void.class, MemoryAddress.class),
                    FunctionDescriptor.ofVoid(C_POINTER)
            );

            dumpPersonHandle.invoke(person.address());

            for (byte b : person.toByteArray()) {
                System.out.print(b + ", ");
            }
            System.out.println();

            MethodHandle onEachHandle = MethodHandles.lookup()
                    .findStatic(ForeignApis.class, "onEach",
                            MethodType.methodType(void.class, int.class));

            MemoryAddress onEachHandleAddress = CLinker.getInstance().upcallStub(
                    onEachHandle, FunctionDescriptor.ofVoid(C_INT), newImplicitScope()
            );

            int[] originalArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            MemorySegment array = MemorySegment.allocateNative(4 * 10, newImplicitScope());
            array.copyFrom(MemorySegment.ofArray(originalArray));

            MemoryAddress forEach = loaderLookup.lookup("ForEach").get();
            MethodHandle forEachHandle = CLinker.getInstance().downcallHandle(
                    forEach,
                    MethodType.methodType(void.class, MemoryAddress.class, int.class, MemoryAddress.class),
                    FunctionDescriptor.ofVoid(C_POINTER, C_INT, C_POINTER)
            );
            forEachHandle.invoke(array.address(), originalArray.length, onEachHandleAddress);


            MemoryAddress strlen = CLinker.systemLookup().lookup("strlen").get();
            MethodHandle strlenHandle = CLinker.getInstance().downcallHandle(strlen,
                    MethodType.methodType(int.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_INT, C_POINTER));

            var string = CLinker.toCString("Hello World!!", ResourceScope.newImplicitScope());
            System.out.println(strlenHandle.invoke(string.address()));
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void useNewMemoryApi() {
        {
            MemorySegment segment = MemorySegment.allocateNative(100, newImplicitScope());
            VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
            for (int i = 0; i < 25; i++) {
                intHandle.set(segment, /* offset */ i * 4, /* value to write */ i);
            }

            // intHandle.set(segment, 100, 1000); // out of bound access.

        }
        try (var scope = ResourceScope.newConfinedScope()) {
            MemorySegment memorySegment = MemorySegment.allocateNative(100, scope);

        }

        {
            SequenceLayout intArrayLayout
                    = MemoryLayout.sequenceLayout(25,
                    MemoryLayout.valueLayout(32, ByteOrder.nativeOrder()));
            MemorySegment segment = MemorySegment.allocateNative(intArrayLayout, newImplicitScope());
            VarHandle indexedElementHandle =
                    intArrayLayout.varHandle(int.class, MemoryLayout.PathElement.sequenceElement());
            for (int i = 0; i < intArrayLayout.elementCount().getAsLong(); i++) {
                indexedElementHandle.set(segment, (long) i, i);
            }

        }

        {
            MethodType mtype = MethodType.methodType(void.class, MemorySegment.class);
            MemoryLayout SYSTEMTIME = MemoryLayout.structLayout(
                    C_SHORT.withName("wYear"), C_SHORT.withName("wMonth"),
                    C_SHORT.withName("wDayOfWeek"), C_SHORT.withName("wDay"),
                    C_SHORT.withName("wHour"), C_SHORT.withName("wMinute"),
                    C_SHORT.withName("wSecond"), C_SHORT.withName("wMilliseconds")
            );
            FunctionDescriptor fdesc = FunctionDescriptor.ofVoid(SYSTEMTIME);
        }
    }

    public static void useUnsafe() {
        try {
            var theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafeField.get(null);
            var handle = unsafe.allocateMemory(16);

            unsafe.putDouble(handle, 1024);
            System.out.println(unsafe.getDouble(handle));

            unsafe.putInt(handle + 16, 1000);
            System.out.println(unsafe.getInt(handle + 16));

            unsafe.freeMemory(handle);
            System.out.println(unsafe.getInt(handle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

