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

package com.pvphall.inject;

import com.google.gson.Gson;
import com.pvphall.inject.config.Config;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Injects {

    private static final Injects instance = new Injects();

    private final Map<String, InjectClass> injectClasses = new HashMap<String, InjectClass>();
    private Config config;

    private Injects() {
        this.config = this.loadConfig();
    }

    private Config loadConfig() {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("injects.json");

            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return new Gson().fromJson(builder.toString(), Config.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addInjectClass(String className, ClassNode node) {
        // Remove the abstract modifier
        node.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;

        this.injectClasses.put(className, new InjectClass(node.methods));
    }

    public MethodNode getInjectForClass(String className, String methodName) {
        InjectClass injectClass = this.injectClasses.get(className);

        if (injectClass != null)
            return injectClass.getMethodNode(methodName);

        return null;
    }

    public Config getConfig() {
        return this.config;
    }

    public static Injects getInstance() {
        return instance;
    }
}
