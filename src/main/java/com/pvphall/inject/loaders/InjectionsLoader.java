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

import com.pvphall.inject.Injects;
import com.pvphall.inject.annotations.Injectable;
import com.pvphall.inject.api.loaders.ILoader;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

public class InjectionsLoader {

    private final Injects injects = Injects.getInstance();
    private Map<String, Class<? extends Injectable>> injectionClasses;
    private ILoader[] loaders;

    public InjectionsLoader() {
        this.injectionClasses = new HashMap<String, Class<? extends Injectable>>();
        this.loaders = new ILoader[] {
            new InterfacesLoader(),
            new MethodsLoader(),
        };
    }

    public void transform(ClassNode node, String className) {
        Class<? extends Injectable> injectable = this.injectionClasses.get(className);

        if (injectable == null)
            return;

        for (ILoader loader : this.loaders)
            loader.transform(node, injectable);
    }

    public void registerInjectionClasses() {
        this.injects
                .getConfig()
                .getInjects()
                .stream()
                .map(this::makeClassInstance)
                .filter(Objects::nonNull)
                .forEach(this::addInjectionClass);
    }

    private Object makeClassInstance(String injectClass) {
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(injectClass);
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println("Failed for " + injectClass);
            e.printStackTrace();
        }

        return null;
    }

    private void addInjectionClass(Object object) {
        Class<?> clazz = object.getClass();
        Injectable injectable = clazz.getAnnotation(Injectable.class);

        if (injectable != null)
            this.injectionClasses.put(injectable.target(), (Class<? extends Injectable>) clazz);
    }
}
