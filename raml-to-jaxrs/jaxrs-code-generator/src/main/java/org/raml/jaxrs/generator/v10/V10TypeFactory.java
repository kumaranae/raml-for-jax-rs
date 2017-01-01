package org.raml.jaxrs.generator.v10;

import com.squareup.javapoet.ClassName;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.EnumerationGenerator;
import org.raml.jaxrs.generator.GProperty;
import org.raml.jaxrs.generator.GType;
import org.raml.jaxrs.generator.GenerationException;
import org.raml.jaxrs.generator.GeneratorType;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.SchemaTypeFactory;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.jaxrs.generator.builders.types.CompositeRamlTypeGenerator;
import org.raml.jaxrs.generator.builders.types.PropertyInfo;
import org.raml.jaxrs.generator.builders.types.RamlTypeGeneratorImplementation;
import org.raml.jaxrs.generator.builders.types.RamlTypeGeneratorInterface;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jean-Philippe Belanger on 12/30/16.
 * Just potential zeroes and ones
 */
class V10TypeFactory {

    static TypeGenerator createObjectType(V10TypeRegistry registry, CurrentBuild currentBuild, V10GType originalType, boolean publicType) {

        List<GType> parentTypes = originalType.parentTypes();
        Map<String, JavaPoetTypeGenerator> internalTypes = new HashMap<>();
        int internalTypeCounter = 0;
        List<PropertyInfo> properties = new ArrayList<>();
        for (GProperty declaration : originalType.properties()) {

            if (declaration.isInternal()) {
                String internalTypeName = Integer.toString(internalTypeCounter);

                V10GType type = registry.createInlineType(internalTypeName, Names.typeName(declaration.name(), "Type"),
                        (TypeDeclaration) declaration.implementation());
                TypeGenerator internalGenerator = inlineTypeBuild(registry, currentBuild, GeneratorType.generatorFrom(type));
                if ( internalGenerator instanceof JavaPoetTypeGenerator ) {
                    internalTypes.put(internalTypeName, (JavaPoetTypeGenerator) internalGenerator);
                    properties.add(new PropertyInfo(declaration.overrideType(type)));
                    internalTypeCounter ++;
                } else {
                    throw new GenerationException("internal type bad");
                }
            } else {
                properties.add(new PropertyInfo(declaration));
            }

        }

        if ( currentBuild.implementationsOnly() ) {

            ClassName impl = buildClassName(currentBuild.getModelPackage(), originalType.defaultJavaTypeName(), publicType);

            RamlTypeGeneratorImplementation implg = new RamlTypeGeneratorImplementation(currentBuild, impl, null, properties, internalTypes, originalType);

            if ( publicType ) {
                currentBuild.newGenerator(originalType.name(), implg);
            }
            return implg;
        } else {

            ClassName interf = buildClassName(currentBuild.getModelPackage(), originalType.defaultJavaTypeName(), publicType);
            ClassName impl = buildClassName(currentBuild.getModelPackage(), originalType.defaultJavaTypeName() + "Impl", publicType);

            RamlTypeGeneratorImplementation implg = new RamlTypeGeneratorImplementation(currentBuild, impl, interf,
                    properties, internalTypes, originalType);
            RamlTypeGeneratorInterface intg = new RamlTypeGeneratorInterface(currentBuild, interf, parentTypes, properties, internalTypes, originalType);
            CompositeRamlTypeGenerator gen = new CompositeRamlTypeGenerator(intg, implg);

            if ( publicType ) {
                currentBuild.newGenerator(originalType.name(), gen);
            }
            return gen;
        }
    }

    static TypeGenerator createEnumerationType(CurrentBuild currentBuild, GType type) {
        JavaPoetTypeGenerator generator =  new EnumerationGenerator(
                currentBuild,
                ((V10GType)type).implementation(),
                ClassName.get(currentBuild.getModelPackage(), type.defaultJavaTypeName()),
                type.enumValues());

        currentBuild.newGenerator(type.name(), generator);
        return generator;
    }

    private static TypeGenerator inlineTypeBuild(V10TypeRegistry registry, CurrentBuild currentBuild, GeneratorType type) {

        switch (type.getObjectType()) {

            case ENUMERATION_TYPE:
                return createEnumerationType(currentBuild, type.getDeclaredType());

            case PLAIN_OBJECT_TYPE:
                return createObjectType(registry, currentBuild, (V10GType) type.getDeclaredType(),  false);

            case JSON_OBJECT_TYPE:
                return SchemaTypeFactory.createJsonType(currentBuild, type.getDeclaredType());

            case XML_OBJECT_TYPE:
                return SchemaTypeFactory.createXmlType(currentBuild, type.getDeclaredType());
        }

        throw new GenerationException("don't know what to do with type " + type.getDeclaredType());
    }

    private static ClassName buildClassName(String pack, String name, boolean publicType) {

        if ( publicType ) {
            return ClassName.get(pack, name);
        } else {

            return ClassName.get("", name);
        }
    }
}