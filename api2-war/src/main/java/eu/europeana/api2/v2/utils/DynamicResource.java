/*
 * Copyright 2007-2018 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.api2.v2.utils;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;

/**
 * Created by luthien on 23/01/2018.
 */
public class DynamicResource implements Resource{
    private String name;

    public DynamicResource(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DynamicResource.class;
    }
    @Override
    public String lookup() {
        return null;
    }

    @Override
    public Class<?> type() {
        return null;
    }

    @Override
    public AuthenticationType authenticationType() {
        return null;
    }

    @Override
    public boolean shareable() {
        return false;
    }

    @Override
    public String mappedName() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }


}
