/*
 * MIT License
 *
 * Copyright (c) 2021 PvPHall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.pvphall.inject.loaders;

import com.pvphall.inject.InjectType;
import com.pvphall.inject.Injects;
import com.pvphall.inject.annotations.Inject;
import com.pvphall.inject.annotations.Injectable;
import com.pvphall.inject.annotations.Precise;
import com.pvphall.inject.annotations.PreciseType;
import com.pvphall.inject.api.loaders.ILoader;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.Iterator;

public class MethodsLoader implements ILoader {

    private final Injects injects = Injects.getInstance();

    @Override
    public void transform(ClassNode node, Class<? extends Injectable> injectable) {
        for (Method method : injectable.getDeclaredMethods()) {
            Inject inject = method.getAnnotation(Inject.class);

            if (inject == null)
                continue;

            String targetMethod = method.getName();
            InjectType injectType = inject.type();
            MethodNode methodNode = node.methods.stream().filter(current -> current.name.equals(targetMethod)).findFirst().orElse(null);
            MethodNode injectMethodNode = this.injects.getInjectForClass(injectable.getName(), targetMethod);

            if (injectMethodNode == null) {
                System.out.println("Inject - Could not find method to inject: " + targetMethod);
                continue;
            }

            switch (injectType) {
                case INSERT_START:
                    this.insertStart(node, injectable, methodNode, injectMethodNode);
                    break;
                case INSERT_END:
                    this.insertEnd(node, injectable, methodNode, injectMethodNode);
                    break;
                case PRECISE:
                    this.precise(node, injectable, inject, methodNode, injectMethodNode);
                    break;
                case REPLACE:
                    this.replace(node, injectable, methodNode, injectMethodNode);
                    break;
                case CREATE:
                    this.createMethod(node, injectable, injectMethodNode);
                    break;
                default:
                    System.out.println("Inject - Unknown inject type: " + injectType);
                    break;
            }
        }
    }

    private void insertStart(ClassNode node, Class<? extends Injectable> injectable, MethodNode method, MethodNode injectMethod) {
        if (method == null) {
            System.out.println("Inject - Target method not found for INSERT_START: " + injectMethod.name);
            return;
        }

        AbstractInsnNode firstNode = method.instructions.getFirst();

        MethodNode newMethodNode = new MethodNode();
        injectMethod.instructions.remove(injectMethod.instructions.getLast());
        injectMethod.instructions.remove(injectMethod.instructions.getLast());
        injectMethod.instructions.accept(newMethodNode);

        InsnList list = this.replaceOwner(newMethodNode.instructions, injectable.getName(), node.name);
        method.instructions.insertBefore(firstNode, list);
    }

    private void insertEnd(ClassNode node, Class<? extends Injectable> injectable, MethodNode method, MethodNode injectMethod) {
        if (method == null) {
            System.out.println("Inject - Target method not found for INSERT_END: " + injectMethod.name);
            return;
        }

        AbstractInsnNode lastNode = method.instructions.get(method.instructions.size() - 2);

        MethodNode newMethodNode = new MethodNode();
        injectMethod.instructions.remove(injectMethod.instructions.getLast());
        injectMethod.instructions.remove(injectMethod.instructions.getLast());
        injectMethod.instructions.accept(newMethodNode);

        InsnList list = this.replaceOwner(newMethodNode.instructions, injectable.getName(), node.name);
        method.instructions.insertBefore(lastNode, list);
    }

    private void precise(ClassNode node, Class<? extends Injectable> injectable, Inject inject, MethodNode method, MethodNode injectMethod) {
        if (method == null) {
            System.out.println("Inject - Target method not found for PRECISE: " + injectMethod.name);
            return;
        }

        Precise[] precise = inject.precise();
        Precise firstPrecise = precise[0];

        if (firstPrecise == null) {
            System.out.println("Inject - PRECISE not found in annotations: " + injectMethod.name);
            return;
        }

        Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();

            if (firstPrecise.type().equals(PreciseType.METHOD) && insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;

                if (methodInsn.name.equals(firstPrecise.name())) {
                    MethodNode newMethodNode = new MethodNode();
                    injectMethod.instructions.remove(injectMethod.instructions.getLast());
                    injectMethod.instructions.remove(injectMethod.instructions.getLast());
                    injectMethod.instructions.accept(newMethodNode);

                    InsnList list = this.replaceOwner(newMethodNode.instructions, injectable.getName(), node.name);
                    method.instructions.insertBefore(methodInsn, list);
                    found = true;
                    break;
                }
            }
        }

        if(!found)
            System.out.println("Inject - PRECISE location not found for method: " + injectMethod.name);
    }

    private void replace(ClassNode node, Class<? extends Injectable> injectable, MethodNode method, MethodNode injectMethod) {
        if (method == null) {
            System.out.println("Inject - Target method not found for REPLACE: " + injectMethod.name);
            return;
        }

        node.methods.remove(method);

        this.createMethod(node, injectable, injectMethod);
    }

    private void createMethod(ClassNode node, Class<? extends Injectable> injectable, MethodNode injectMethodNode) {
        MethodNode newMethodNode = new MethodNode(
                injectMethodNode.access,
                injectMethodNode.name,
                injectMethodNode.desc,
                injectMethodNode.signature,
                injectMethodNode.exceptions.toArray(new String[0])
        );

        InsnList list = this.replaceOwner(injectMethodNode.instructions, injectable.getName(), node.name);

        newMethodNode.instructions.insert(list);
        node.methods.add(newMethodNode);
    }

    private InsnList replaceOwner(InsnList list, String injectOwner, String targetOwner) {
        Iterator<AbstractInsnNode> iterator = list.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();
            String injectableName = injectOwner.replace(".", "/");

            if(next instanceof FieldInsnNode) {
                FieldInsnNode fieldNext = (FieldInsnNode) next;

                if (fieldNext.owner.equals(injectableName))
                    fieldNext.owner = targetOwner;
                else if (fieldNext.owner.contains("$")) {
                    String[] owners = fieldNext.owner.split("\\$");
                    String firstOwner = owners[0];
                    String secondOwner = owners[1];

                    if (firstOwner.equals(injectableName))
                        fieldNext.owner = targetOwner + "$" + secondOwner;
                }
            } else if(next instanceof MethodInsnNode) {
                MethodInsnNode methodNext = (MethodInsnNode) next;

                if (methodNext.owner.equals(injectableName))
                    methodNext.owner = targetOwner;
            }
        }

        return list;
    }
}

