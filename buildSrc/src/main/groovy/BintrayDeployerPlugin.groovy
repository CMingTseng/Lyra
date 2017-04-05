///*
// * Copyright (c) 2017 Fondesa
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.api.artifacts.Dependency
//import org.gradle.api.artifacts.ExcludeRule
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.api.tasks.bundling.Jar
//import org.gradle.api.tasks.javadoc.Javadoc
//
//class BintrayDeployerPlugin implements Plugin<Project> {
//
//    /**
//     * Name of the properties file without extension.
//     */
//    private static final def FILE_NAME = "gradle"
//
//    @Override
//    void apply(Project project) {
//
//            // Load Bintray deploy properties file.
//            Properties props = new Properties()
//            props.load(new FileInputStream("${FILE_NAME}.properties"))
//
//            // Android libraries
//            task("sourcesJar", type: Jar) {
//                classifier = 'sources'
//                from android.sourceSets.main.java.srcDirs
//            }
//
//            task("javadoc", type: Javadoc) {
//                failOnError false
//                source = android.sourceSets.main.java.srcDirs
//                classpath += project.files(android.getBootClasspath().join(File.pathSeparator)) + configurations.compile
//            }
//
//            task("javadocJar", type: Jar, dependsOn: javadoc) {
//                classifier = 'javadoc'
//                from javadoc.destinationDir
//            }
//
//            artifacts {
//                archives javadocJar
//                archives sourcesJar
//            }
//
//            group = project.hasProperty("BINTRAY_COMMON_GROUP_ID") ? BINTRAY_COMMON_GROUP_ID : ""
//            version = props["BINTRAY_LIB_VERSION"]
//
//            apply plugin: 'com.jfrog.bintray'
//            apply plugin: 'maven-publish'
//
//            // Create the publication with the pom configuration:
//            publishing {
//                publications {
//                    bintrayPublication(MavenPublication) {
//                        artifact sourcesJar
//                        artifact javadocJar
//                        artifact("$buildDir/outputs/aar/${project.name}-release.aar")
//
//                        groupId project.hasProperty("BINTRAY_COMMON_GROUP_ID") ? BINTRAY_COMMON_GROUP_ID : ""
//                        artifactId props["BINTRAY_LIB_ARTIFACT_ID"]
//                        version props["BINTRAY_LIB_VERSION"]
//
//                        pom.withXml {
//                            def root = asNode()
//
//                            // Add general lib's information
//                            root.appendNode('name', props["BINTRAY_LIB_NAME"])
//                            root.appendNode('description', props["BINTRAY_LIB_DESCRIPTION"])
//                            root.appendNode('url', props["BINTRAY_LIB_SITE_URL"])
//
//                            // Add license part
//                            def licensesNode = root.appendNode('licenses')
//                            def licenseNode = licensesNode.appendNode('license')
//                            def licenseUrl = project.hasProperty("BINTRAY_COMMON_LICENSE_URL") ? BINTRAY_COMMON_LICENSE_URL : ""
//                            licenseNode.appendNode('url', licenseUrl)
//
//                            // Add developers part
//                            def developersNode = root.appendNode('developers')
//                            def developerNode = developersNode.appendNode('developer')
//
//                            def developerId = project.hasProperty("BINTRAY_COMMON_DEV_ID") ? BINTRAY_COMMON_DEV_ID : ""
//                            def developerName = project.hasProperty("BINTRAY_COMMON_DEV_NAME") ? BINTRAY_COMMON_DEV_NAME : ""
//                            def developerEmail = project.hasProperty("BINTRAY_COMMON_DEV_MAIL") ? BINTRAY_COMMON_DEV_MAIL : ""
//
//                            developerNode.appendNode('id', developerId)
//                            developerNode.appendNode('name', developerName)
//                            developerNode.appendNode('email', developerEmail)
//
//                            // Add SCM part
//                            def scmNode = root.appendNode('scm')
//                            scmNode.appendNode('connection', props["BINTRAY_LIB_GIT_URL"])
//                            scmNode.appendNode('developerConnection', props["BINTRAY_LIB_GIT_URL"])
//                            scmNode.appendNode('url', props["BINTRAY_LIB_SITE_URL"])
//
//                            // Add dependencies part
//                            def dependenciesNode = root.appendNode('dependencies')
//
//                            // List all compile dependencies and write to POM
//                            configurations.compile.getAllDependencies().each { Dependency dep ->
//                                if (dep.group == null || dep.version == null)
//                                    return // ignore invalid dependencies
//
//                                def dependencyVersion = dep.name
//                                if (dependencyVersion == null || dependencyVersion == "unspecified")
//                                    dependencyVersion = props["BINTRAY_LIB_VERSION"]
//
//                                def dependencyNode = dependenciesNode.appendNode('dependency')
//                                dependencyNode.appendNode('groupId', dep.group)
//                                dependencyNode.appendNode('artifactId', dependencyVersion)
//                                dependencyNode.appendNode('version', dep.version)
//
//                                if (!dep.transitive) {
//                                    // If this dependency is transitive, we should force exclude all its dependencies them from the POM
//                                    def exclusionNode = dependencyNode.appendNode('exclusions').appendNode('exclusion')
//                                    exclusionNode.appendNode('groupId', '*')
//                                    exclusionNode.appendNode('artifactId', '*')
//                                } else if (!dep.properties.excludeRules.empty) {
//                                    // Otherwise add specified exclude rules
//                                    def exclusionsNode = dependencyNode.appendNode('exclusions')
//                                    dep.properties.excludeRules.each { ExcludeRule rule ->
//                                        def exclusionNode = exclusionsNode.appendNode('exclusion')
//                                        exclusionNode.appendNode('groupId', rule.group ?: '*')
//                                        exclusionNode.appendNode('artifactId', rule.module ?: '*')
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            bintray {
//                user = project.hasProperty("BINTRAY_COMMON_USERNAME") ? BINTRAY_COMMON_USERNAME : ""
//                key = project.hasProperty("BINTRAY_COMMON_APIKEY") ? BINTRAY_COMMON_APIKEY : ""
//
//                publications = ['bintrayPublication']
//                pkg {
//                    repo = project.hasProperty("BINTRAY_COMMON_REPO") ? BINTRAY_COMMON_REPO : ""
//                    name = BINTRAY_LIB_REPO_NAME
//                    desc = BINTRAY_LIB_DESCRIPTION
//                    websiteUrl = BINTRAY_LIB_SITE_URL
//                    issueTrackerUrl = BINTRAY_LIB_ISSUE_TRACKER_URL
//                    vcsUrl = BINTRAY_LIB_GIT_URL
//                    licenses = [project.hasProperty("BINTRAY_COMMON_LICENSE_ID") ? BINTRAY_COMMON_LICENSE_ID : ""]
//                    publish = true
//                    publicDownloadNumbers = true
//                    version {
//                        released = new Date()
//                        gpg {
//                            sign = true
//                            passphrase = project.hasProperty("BINTRAY_COMMON_GPG_PASSWORD") ? BINTRAY_COMMON_GPG_PASSWORD : ""
//                        }
//                    }
//                }
//            }
//
//            def addTaskToMap = { taskMap, taskName ->
//                taskMap.put(taskName, tasks.findByName(taskName) != null)
//            }
//
//            final def TASK_BINTRAY = "bintrayUpload"
//            final def TASK_CLEAN = "clean"
//            final def TASK_ASSEMBLE = "assembleRelease"
//            final def TASK_SOURCES = "sourcesJar"
//            final def TASK_JAVADOC = "javadocJar"
//            final def TASK_POM = "generatePomFileForBintrayPublicationPublication"
//
//            def taskMap = new HashMap<String, Boolean>()
//            addTaskToMap(taskMap, TASK_BINTRAY)
//            addTaskToMap(taskMap, TASK_CLEAN)
//            addTaskToMap(taskMap, TASK_ASSEMBLE)
//            addTaskToMap(taskMap, TASK_SOURCES)
//            addTaskToMap(taskMap, TASK_JAVADOC)
//            addTaskToMap(taskMap, TASK_POM)
//
//            tasks.whenTaskAdded { task ->
//                def taskName = task.name
//                def mapTaskInserted = taskMap.get(taskName)
//                if (mapTaskInserted != null && !mapTaskInserted) {
//                    taskMap.put(taskName, true)
//                    println("Inserted: $taskName")
//                    def allInserted = true
//                    for (Map.Entry<String, Boolean> entry : taskMap.entrySet()) {
//                        allInserted = entry.value
//                        if (!allInserted)
//                            break
//                    }
//                    if (allInserted) {
//                        def newTask = project.task("publishLibrary")
//                        newTask.group = "publishing"
//
//                        newTask.dependsOn(TASK_CLEAN)
//                        newTask.dependsOn(TASK_ASSEMBLE)
//                        newTask.dependsOn(TASK_SOURCES)
//                        newTask.dependsOn(TASK_JAVADOC)
//                        newTask.dependsOn(TASK_POM)
//
//                        tasks.findByName(TASK_ASSEMBLE).mustRunAfter TASK_CLEAN
//                        tasks.findByName(TASK_SOURCES).mustRunAfter TASK_ASSEMBLE
//                        tasks.findByName(TASK_JAVADOC).mustRunAfter TASK_SOURCES
//                        tasks.findByName(TASK_POM).mustRunAfter TASK_JAVADOC
//
//                        newTask.finalizedBy TASK_BINTRAY
//                    }
//                }
//            }
//    }
//}