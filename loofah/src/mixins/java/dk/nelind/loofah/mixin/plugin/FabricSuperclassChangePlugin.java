/*
 * This file is part of Loofah, licensed under the MIT License (MIT).
 *
 * Copyright (c) Nelind <https://www.nelind.dk>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.nelind.loofah.mixin.plugin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.common.mixin.plugin.AbstractMixinConfigPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class FabricSuperclassChangePlugin extends AbstractMixinConfigPlugin {
    @Override
    public void preApply(
        final String targetClassName,
        final ClassNode targetClass,
        final String mixinClassName,
        final IMixinInfo mixinInfo
    ) {
        FabricSuperclassChangePlugin.transform(
            targetClass,
            mixinInfo.getClassNode(0)
        );
    }

    /**
     * Annotation used by {@link FabricSuperclassChangePlugin} to determine what super class change to perform
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    public @interface ChangeSuperclass {
        Class<?> value();
    }

    // core super class change logic adapted from org.spongepowered.transformers.modlauncher.SuperclassChanger
    private static void transform(final ClassNode targetClass, final ClassNode mixinClass) {
        AnnotationNode ann = Annotations.getInvisible(mixinClass, ChangeSuperclass.class);
        String newSuperclass = ((Type) Annotations.getValue(ann, "value")).getClassName();
        final String sanitizedSuperClass = newSuperclass.replace('.', '/');

        targetClass.methods.forEach(m -> FabricSuperclassChangePlugin.transformMethod(m, targetClass.superName, sanitizedSuperClass));
        targetClass.superName = sanitizedSuperClass;
    }

    private static void transformMethod(final MethodNode node, final String originalSuperclass, final String superClass) {
        for (final MethodInsnNode insn : FabricSuperclassChangePlugin.findSuper(node, originalSuperclass)) {
            insn.owner = superClass;
        }
    }

    private static List<MethodInsnNode> findSuper(final MethodNode method, final String originalSuperClass) {
        final List<MethodInsnNode> nodes = new ArrayList<>();
        for (final AbstractInsnNode node : method.instructions.toArray()) {
            if (node.getOpcode() == Opcodes.INVOKESPECIAL && originalSuperClass.equals(
                ((MethodInsnNode) node).owner)) {
                nodes.add((MethodInsnNode) node);
            }
        }
        return nodes;
    }
}
