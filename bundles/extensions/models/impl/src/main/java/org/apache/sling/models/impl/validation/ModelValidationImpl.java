/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.models.impl.validation;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.InvalidResourceException;
import org.apache.sling.models.factory.InvalidValidationModelException;
import org.apache.sling.validation.api.ValidationModel;
import org.apache.sling.validation.api.ValidationResult;
import org.apache.sling.validation.api.ValidationService;
import org.apache.sling.validation.api.exceptions.SlingValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component
public class ModelValidationImpl implements ModelValidation {

    @Reference
    private ValidationService validation;
    
    private static final Logger log = LoggerFactory.getLogger(ModelValidationImpl.class);
    
    @Override
    public RuntimeException validate(Resource resource, boolean required) {
        try {
            ValidationModel validationModel = validation.getValidationModel(resource);
            if (validationModel == null) {
                String error = String.format("Could not find validation model for resource '%s' with type '%s'", resource.getPath(), resource.getResourceType());
                if (required) {
                    return new InvalidValidationModelException(error);
                } else {
                    log.warn(error);
                }
            } else {
                try {
                    ValidationResult validationResult = validation.validate(resource, validationModel);
                    if (!validationResult.isValid()) {
                        return new InvalidResourceException(validationResult, resource.getPath());
                    } 
                } catch (SlingValidationException e) {
                    return new InvalidValidationModelException(e);
                }
            }
        } catch (IllegalStateException e) {
            return new InvalidValidationModelException(e);
        }
        
        return null;
    }

    
}
