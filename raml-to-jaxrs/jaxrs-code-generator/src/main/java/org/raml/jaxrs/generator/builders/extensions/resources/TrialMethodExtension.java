package org.raml.jaxrs.generator.builders.extensions.resources;

import com.squareup.javapoet.MethodSpec;
import org.raml.jaxrs.generator.extension.Context;
import org.raml.jaxrs.generator.extension.MethodExtension;
import org.raml.jaxrs.generator.v10.V10GMethod;

/**
 * Created by Jean-Philippe Belanger on 1/6/17.
 * Just potential zeroes and ones
 */
public class TrialMethodExtension implements MethodExtension {

    @Override
    public MethodSpec.Builder onMethod(Context context, V10GMethod method, MethodSpec.Builder methodSpec) {
        return methodSpec;
    }


}
