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

import com.pvphall.inject.annotations.Injectable;
import com.pvphall.inject.api.loaders.ILoader;
import org.objectweb.asm.tree.ClassNode;

public class InterfacesLoader implements ILoader {

    @Override
    public void transform(ClassNode node, Class<? extends Injectable> injectable) {
        for (Class<?> current : injectable.getInterfaces()) {
            String interfaceName = current.getName().replace(".", "/");

            if (!node.interfaces.contains(interfaceName))
                node.interfaces.add(interfaceName);
        }
    }
}
