package cn.edu.jxnu.reflect.asm;

/***
 * ASM examples: examples showing how ASM can be used
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * @author Eric Bruneton
 */
public class Helloworld extends ClassLoader implements Opcodes {

	@SuppressWarnings("deprecation")
	public static void main(final String args[]) throws Exception {

		// Generates the bytecode corresponding to the following Java class:
		//
		// public class Example {
		// public static void main (String[] args) {
		// System.out.println("Hello world!");
		// }
		// }

		// 创建一个Example类的 ClassWriter
		// which inherits from Object
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_1, ACC_PUBLIC, "Example", null, "java/lang/Object", null);

		// 创建默认构造方法的MethodWriter
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		// 将this压入栈
		mw.visitVarInsn(ALOAD, 0);
		// 调超类构造
		mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mw.visitInsn(RETURN);
		// 此代码最多使用一个堆栈元素和一个局部变量
		mw.visitMaxs(1, 1);
		mw.visitEnd();

		// 为‘main’方法创建一个方法编写器
		mw = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// System的out入栈
		mw.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		// "Hello World!" 字符串常量入栈
		mw.visitLdcInsn("Hello world!");
		// 调用println方法
		mw.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		mw.visitInsn(RETURN);
		// 此代码最多使用两个堆栈元素和两个本地元素。
		mw.visitMaxs(2, 2);
		mw.visitEnd();

		// 获取示例类的字节码，并动态加载它。
		byte[] code = cw.toByteArray();

		FileOutputStream fos = new FileOutputStream(
				"D:\\git_project\\Java-Learning-Summary\\Java-Learning-Summary\\src\\cn\\edu\\jxnu\\reflect\\asm\\Example.class");
		fos.write(code);
		fos.close();

		Helloworld loader = new Helloworld();
		Class<?> exampleClass = loader.defineClass("Example", code, 0, code.length);

		// 使用动态生成的类打印“HelloWorld”
		exampleClass.getMethods()[0].invoke(null, new Object[] { null });

		// ------------------------------------------------------------------------
		// 与GeneratorAdapter相同的示例(更方便，但更慢)
		// ------------------------------------------------------------------------

		cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_1, ACC_PUBLIC, "Example", null, "java/lang/Object", null);

		// 创建默认构造器的GeneratorAdapter
		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(Object.class), m);
		mg.returnValue();
		mg.endMethod();

		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
		mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
		mg.push("Hello world!");
		mg.invokeVirtual(Type.getType(PrintStream.class), Method.getMethod("void println (String)"));
		mg.returnValue();
		mg.endMethod();

		cw.visitEnd();

		code = cw.toByteArray();
		loader = new Helloworld();
		exampleClass = loader.defineClass("Example", code, 0, code.length);

		exampleClass.getMethods()[0].invoke(null, new Object[] { null });
	}
}