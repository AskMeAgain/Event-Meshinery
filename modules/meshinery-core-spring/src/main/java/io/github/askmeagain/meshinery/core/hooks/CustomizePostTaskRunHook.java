package io.github.askmeagain.meshinery.core.hooks;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.function.Consumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
@FunctionalInterface
public interface CustomizePostTaskRunHook extends Consumer<MeshineryDataContext> {
}
