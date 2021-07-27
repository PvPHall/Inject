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

package com.pvphall.inject.transformers;

import com.pvphall.inject.Injects;
import com.pvphall.inject.api.transformers.AbstractClassTransformer;
import com.pvphall.inject.loaders.InjectionsLoader;
import org.objectweb.asm.tree.ClassNode;

public class DetectInjectClassTransformer extends AbstractClassTransformer {

    private final Injects injects = Injects.getInstance();

    @Override
    public void transform(ClassNode node, String className) {
        if (this.injects.getConfig().getInjects().contains(className))
            this.injects.addInjectClass(className, node);
    }
}

