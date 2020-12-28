package dev.nokee.platform.c;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;

/**
 * Configuration for an application written in C, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the C Application Plugin.</p>
 *
 * @since 0.4
 */
@Deprecated // Use CApplication instead
public interface CApplicationExtension extends DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<CApplicationSources>, HasPrivateHeaders, HasCSources {}
