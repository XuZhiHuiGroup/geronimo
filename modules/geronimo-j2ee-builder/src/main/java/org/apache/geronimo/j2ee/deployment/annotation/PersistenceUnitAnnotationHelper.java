/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee.XsdAnyURIType;
import org.apache.xbean.finder.ClassFinder;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@PersistenceUnit</strong> and <strong>@PersistenceUnits</strong> annotations to deployment
 * descriptor tags. The PersistenceUnitAnnotationHelper class can be used as part of the deployment of
 * a module into the Geronimo server. It performs the following major functions:
 * <p/>
 * <ol>
 * <li>Translates annotations into corresponding deployment descriptor elements (so that the
 * actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 * <p/>
 * <p><strong>Note(s):</strong>
 * <ul>
 * <li>The user is responsible for invoking change to metadata-complete
 * <li>This helper class will validate any changes it makes to the deployment descriptor. An
 * exception will be thrown if it fails to parse
 * </ul>
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>None
 * </ul>
 *
 * @version $Rev $Date
 * @since 04-2007
 */
public final class PersistenceUnitAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog(PersistenceUnitAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private PersistenceUnitAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the PersistenceUnit and PersistenceUnits annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        if (annotatedApp != null) {
            if (classFinder.isAnnotationPresent(PersistenceUnits.class)) {
                processPersistenceUnits(annotatedApp, classFinder);
            }
            if (classFinder.isAnnotationPresent(PersistenceUnit.class)) {
                processPersistenceUnit(annotatedApp, classFinder);
            }
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processPersistenceUnit(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceUnit(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class> classeswithPersistenceUnit = classFinder.findAnnotatedClasses(PersistenceUnit.class);
        List<Method> methodswithPersistenceUnit = classFinder.findAnnotatedMethods(PersistenceUnit.class);
        List<Field> fieldswithPersistenceUnit = classFinder.findAnnotatedFields(PersistenceUnit.class);

        // Class-level annotation
        for (Class cls : classeswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = (PersistenceUnit) cls.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = method.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = field.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
        validateDD(annotatedApp);

        log.debug("processPersistenceUnit(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processPersistenceUnits(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceUnits(): Entry");

        List<Class> classeswithPersistenceUnits = classFinder.findAnnotatedClasses(PersistenceUnits.class);

        // Class-level annotation(s)
        List<PersistenceUnit> persistenceUnitList = new ArrayList<PersistenceUnit>();
        for (Class cls : classeswithPersistenceUnits) {
            PersistenceUnits persistenceUnits = (PersistenceUnits) cls.getAnnotation(PersistenceUnits.class);
            if (persistenceUnits != null) {
                persistenceUnitList.addAll(Arrays.asList(persistenceUnits.value()));
            }
            for (PersistenceUnit persistenceUnit : persistenceUnitList) {
                addPersistenceUnit(annotatedApp, persistenceUnit, cls, null, null);
            }
            persistenceUnitList.clear();
        }

        log.debug("processPersistenceUnits(): Exit");
    }


    /**
     * Add @PersistenceUnit and @PersistenceUnits annotations to the deployment descriptor. XMLBeans are used to
     * read and manipulate the deployment descriptor as necessary. The PersistenceUnit annotation(s) will be
     * converted to one of the following deployment descriptors:
     *
     * <ol>
     *      <li><persistence-unit-ref> -- Describes a single entity manager factory reference for the
     *          persistence unit
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *          existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @PersistenceUnit annotation
     * @param cls        Class name with the @PersistenceUnit annoation
     * @param method     Method name with the @PersistenceUnit annoation
     * @param field      Field name with the @PersistenceUnit annoation
     * @param annotatedApp  Access to the specc dd
     */
    private static void addPersistenceUnit(AnnotatedApp annotatedApp, PersistenceUnit annotation, Class cls, Method method, Field field) {
        log.debug("addPersistenceUnit( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // PersistenceUnitRef name:
        // -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   Name is JavaBeans property name qualified
        //                                              by the class (or as provided on the
        //                                              annotation)
        // -- When annotation is applied on a field:    Name is the field name qualified by the
        //                                              class (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String persistenceUnitRefName = annotation.name();
        if (persistenceUnitRefName.equals("")) {
            if (method != null) {
                StringBuilder stringBuilder = new StringBuilder(method.getName().substring(3));
                stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
                persistenceUnitRefName = method.getDeclaringClass().getName() + "/" + stringBuilder.toString();
            } else if (field != null) {
                persistenceUnitRefName = field.getDeclaringClass().getName() + "/" + field.getName();
            }
        }
        log.debug("addPersistenceUnit(): persistenceUnitRefName: " + persistenceUnitRefName);

        // If there is already xml for the persistence unit ref, just add injection targets and return.
        PersistenceUnitRefType[] persistenceUnitRefs = annotatedApp.getPersistenceUnitRefArray();
        for (PersistenceUnitRefType persistenceUnitRef : persistenceUnitRefs) {
            if (persistenceUnitRef.getPersistenceUnitRefName().getStringValue().trim().equals(persistenceUnitRefName)) {
                if (method != null || field != null) {
                    InjectionTargetType[] targets = persistenceUnitRef.getInjectionTargetArray();
                    if (!hasTarget(method, field, targets)) {
                        configureInjectionTarget(persistenceUnitRef.addNewInjectionTarget(), method, field);
                    }
                }
                return;
            }
        }

        // Doesn't exist in deployment descriptor -- add new
        PersistenceUnitRefType persistenceUnitRef = annotatedApp.addNewPersistenceUnitRef();

        //------------------------------------------------------------------------------
        // <persistence-unit-ref> required elements:
        //------------------------------------------------------------------------------

        // persistence-unit-ref-name
        JndiNameType unitRefName = persistenceUnitRef.addNewPersistenceUnitRefName();
        unitRefName.setStringValue(persistenceUnitRefName);

        //------------------------------------------------------------------------------
        // <persistence-unit-ref> optional elements:
        //------------------------------------------------------------------------------

        // persistence-unit-name
        String unitNameAnnotation = annotation.unitName();
        if (!unitNameAnnotation.equals("")) {
            org.apache.geronimo.xbeans.javaee.String persistenceUnitName = persistenceUnitRef.addNewPersistenceUnitName();
            persistenceUnitName.setStringValue(unitNameAnnotation);
        }

        // injection targets
        if (method != null || field != null) {
            configureInjectionTarget(persistenceUnitRef.addNewInjectionTarget(), method, field);
        }

    }

}