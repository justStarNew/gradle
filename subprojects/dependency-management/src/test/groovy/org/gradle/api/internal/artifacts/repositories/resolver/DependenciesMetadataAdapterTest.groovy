/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.repositories.resolver

import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.internal.notations.DependencyMetadataNotationParser
import org.gradle.internal.component.external.model.GradleDependencyMetadata
import org.gradle.internal.component.model.DependencyMetadata
import org.gradle.internal.reflect.DirectInstantiator
import spock.lang.Specification

import static org.gradle.internal.component.external.model.DefaultModuleComponentSelector.newSelector

class DependenciesMetadataAdapterTest extends Specification {
    List<DependencyMetadata> dependenciesMetadata = []
    TestDependenciesMetadataAdapter adapter

    def setup() {
        fillDependencyList(0)
    }

    def "add via string id is propagate to the underlying dependency list"() {
        when:
        adapter.add "org.gradle.test:module1:1.0"

        then:
        dependenciesMetadata.size() == 1
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "1.0"
    }

    def "add via map id propagate to the underlying dependency list"() {
        when:
        adapter.add group: "org.gradle.test", name: "module1", version: "1.0"

        then:
        dependenciesMetadata.size() == 1
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "1.0"
    }

    def "add via string id with action is propagate to the underlying dependency list"() {
        when:
        adapter.add("org.gradle.test:module1") {
            it.version { it.prefer '1.0' }
        }

        then:
        dependenciesMetadata.size() == 1
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "1.0"
    }

    def "add via map id with action propagate to the underlying dependency list"() {
        when:
        adapter.add(group: "org.gradle.test", name: "module1") {
            it.version { it.prefer '1.0' }
        }

        then:
        dependenciesMetadata.size() == 1
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "1.0"
    }

    def "remove is propagated to the underlying dependency list"() {
        given:
        fillDependencyList(1)

        when:
        adapter.removeAll { true }

        then:
        dependenciesMetadata == []
    }

    def "can add a dependency with the same coordinates more than once"() {
        //this test documents given behavior, which is not necessarily needed
        given:
        fillDependencyList(1)

        when:
        adapter.add("org.gradle.test:module1:2.0")

        then:
        dependenciesMetadata.size() == 2
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "1.0"
        dependenciesMetadata[1].selector.group == "org.gradle.test"
        dependenciesMetadata[1].selector.module == "module1"
        dependenciesMetadata[1].selector.version == "2.0"
    }

    def "adapters for list items are created lazily"() {
        when:
        fillDependencyList(2)

        then:
        dependenciesMetadata.size() == 2
        adapter.dependencyMetadataAdapters.size() == 0

        when:
        ++adapter.iterator()

        then:
        dependenciesMetadata.size() == 2
        adapter.dependencyMetadataAdapters.size() == 1

        when:
        adapter.each {}

        then:
        dependenciesMetadata.size() == 2
        adapter.dependencyMetadataAdapters.size() == 2
    }

    def "size check is propagated to the underlying dependency list"() {
        when:
        fillDependencyList(3)

        then:
        dependenciesMetadata.size() == 3
        adapter.size() == 3
        adapter.dependencyMetadataAdapters.size() == 0
    }

    def "iterator returns views on underlying list items"() {
        given:
        fillDependencyList(1)

        when:
        def dependencyMetadata = ++adapter.iterator()

        then:
        dependencyMetadata instanceof DirectDependencyMetadataAdapter
        !(dependencyMetadata instanceof DirectDependencyMetadataImpl)
    }

    def "can modify underlying list items"() {
        given:
        fillDependencyList(1)

        when:
        adapter.get(0).version { it.prefer "2.0" }

        then:
        dependenciesMetadata.size() == 1
        dependenciesMetadata[0].selector.group == "org.gradle.test"
        dependenciesMetadata[0].selector.module == "module1"
        dependenciesMetadata[0].selector.version == "2.0"
    }

    private fillDependencyList(int size) {
        dependenciesMetadata = []
        for (int i = 0; i < size; i++) {
            ModuleComponentSelector requested = newSelector("org.gradle.test", "module$size", "1.0")
            dependenciesMetadata += [ new GradleDependencyMetadata(requested, [], null) ]
        }
        adapter = new TestDependenciesMetadataAdapter(dependenciesMetadata)
    }

    class TestDependenciesMetadataAdapter extends AbstractDependenciesMetadataAdapter {
        TestDependenciesMetadataAdapter(List<DependencyMetadata> dependenciesMetadata) {
            super(dependenciesMetadata, DirectInstantiator.INSTANCE, DependencyMetadataNotationParser.parser(DirectInstantiator.INSTANCE, DirectDependencyMetadataImpl.class))
        }

        @Override
        protected Class adapterImplementationType() {
            return DirectDependencyMetadataAdapter
        }

        @Override
        protected boolean isPending() {
            return false
        }
    }
}
