import utils.Constants

def project = Constants.PROJECT
def services = Constants.NODEJS_BACKEND
def environments = Constants.ENVIRONMENTS
def ecrUrl = Constants.ECR_URL
def awsAccountsId = Constants.AWS_ACCOUNTS_ID
def awsAccountsRegion = Constants.AWS_ACCOUNTS_REGION
def telegramNotification = Constants.TELEGRAM_NOTIFICATION
def canDeployProdUsers = Constants.CAN_DEPLOY_PROD_USERS
def kanikoVersion = Constants.KANIKO_VERSION

// Each service has its own folder
def createFolder(service) {
    def repoFolder = service.name
    folder(repoFolder) {
        displayName(repoFolder)
        description('Jobs that are automatically generated for ' + repoFolder)
    }

    return repoFolder
}

// Build job
//  - build docker image
//  - call deploy-dev job
def createBuildJob(project, repoFolder, service, ecrUrl, telegramNotification, kanikoVersion) {
    pipelineJob(repoFolder + "/" + service.name + "-build") {

        parameters {
            stringParam('BRANCH_TO_BUILD', 'main', 'Choose which branch to build, main branch by default')
        }

        // properties {
        //     disableConcurrentBuilds()
        // }

        def pipelineContent = readFileFromWorkspace("lib/jobs/core/jenkinsForNodejs/JenkinsfileBuild")
        pipelineContent = pipelineContent.replace("{{ PROJECT_NAME }}", "$project")
        pipelineContent = pipelineContent.replace("{{ SERVICE_NAME }}", "$service.name")
        pipelineContent = pipelineContent.replace("{{ SERVICE_REPO_URL }}", "$service.repo_url")
        pipelineContent = pipelineContent.replace("{{ ECR_URL }}", "$ecrUrl")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_NOTIFICATION_ENABLED }}", "${telegramNotification['enabled']}")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_CHAT_ID }}", "${telegramNotification['chatId']}")
        pipelineContent = pipelineContent.replace("{{ KANIKO_VERSION }}", "${kanikoVersion}")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_BOT_TOKEN }}", "${telegramNotification['telegramBotToken']}")

        definition {
            cps {
                script(pipelineContent)
                sandbox()
            }
        }

        logRotator {
            numToKeep(30)
        }

        triggers {
            genericTrigger {
                genericVariables {
                    genericVariable {
                        key("ref")
                        value("\$.pull_request.base.ref")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                    genericVariable {
                        key("merged")
                        value("\$.pull_request.merged")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                    genericVariable {
                        key("action")
                        value("\$.action")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                }
                token("$service.name")
                tokenCredentialId('')
                printContributedVariables(true)
                printPostContent(true)
                silentResponse(false)
                shouldNotFlattern(false)
                regexpFilterText("\$ref-\$merged-\$action")
                regexpFilterExpression("main-true-closed")
            }
        }
    }
}

def createBuildJobSpecificBranch(project, repoFolder, service, branchToBuild) {
    pipelineJob(repoFolder + "/" + service.name + "-build-" + branchToBuild) {
        authorization {
            blocksInheritance()
        }

        properties {
            disableConcurrentBuilds()
        }

        def pipelineContent = readFileFromWorkspace("lib/jobs/core/commonJenkinsfile/JenkinsfileBuildSpecificBranch")
        pipelineContent = pipelineContent.replace("{{ PROJECT_NAME }}", "$project")
        pipelineContent = pipelineContent.replace("{{ SERVICE_NAME }}", "$service.name")
        pipelineContent = pipelineContent.replace("{{ SERVICE_REPO_URL }}", "$service.repo_url")
        pipelineContent = pipelineContent.replace("{{ BRANCH_TO_BUILD }}", "$branchToBuild")

        definition {
            cps {
                script(pipelineContent)
                sandbox()
            }
        }

        logRotator {
            numToKeep(30)
        }

        triggers {
            genericTrigger {
                genericVariables {
                    genericVariable {
                        key("ref")
                        value("\$.pull_request.base.ref")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                    genericVariable {
                        key("merged")
                        value("\$.pull_request.merged")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                    genericVariable {
                        key("action")
                        value("\$.action")
                        expressionType("JSONPath") //Optional, defaults to JSONPath
                        regexpFilter("") //Optional, defaults to empty string
                        defaultValue("") //Optional, defaults to empty string
                    }
                }
                token("$service.name-on-specific-branch")
                tokenCredentialId('')
                printContributedVariables(true)
                printPostContent(true)
                silentResponse(false)
                shouldNotFlattern(false)
                regexpFilterText("\$ref-\$merged-\$action")
                regexpFilterExpression("$branchToBuild-true-closed")
            }
        }
    }
}

// Deploy job
// Deploy a service helm-chart to different environment (dev, qa, staging, prod)
// awsAccountId according to environment
// awsRegion also according to environment
def createDeployJob(project, repoFolder, service, environment, ecrUrl, awsAccountId, awsRegion, telegramNotification, canDeployProdUsers) {
    pipelineJob(repoFolder + "/" + service.name + "-deploy-" + environment) {

        parameters {
            stringParam('VERSION', '', 'The version to deploy of the service. Example: 1.0.1')
        }

        properties {
            disableConcurrentBuilds()
        }

        def pipelineContent = readFileFromWorkspace("lib/jobs/core/jenkinsForNodejs/JenkinsfileDeploy")
        pipelineContent = pipelineContent.replace("{{ PROJECT_NAME }}", "$project")
        pipelineContent = pipelineContent.replace("{{ SERVICE_NAME }}", "$service.name")
        pipelineContent = pipelineContent.replace("{{ IMAGE_NAME }}", "$service.image_name")
        pipelineContent = pipelineContent.replace("{{ SERVICE_REPO_URL }}", "$service.repo_url")
        pipelineContent = pipelineContent.replace("{{ ENVIRONMENT_NAME }}", "$environment")
        pipelineContent = pipelineContent.replace("{{ ECR_URL }}", "$ecrUrl")
        pipelineContent = pipelineContent.replace("{{ AWS_ACCOUNT_ID }}", "$awsAccountId")
        pipelineContent = pipelineContent.replace("{{ AWS_REGION }}", "$awsRegion")
        pipelineContent = pipelineContent.replace("{{ HELMCHART_OCI }}", "$service.helmchart_oci")
        pipelineContent = pipelineContent.replace("{{ HELMCHART_VERSION }}", "$service.helmchart_version")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_NOTIFICATION_ENABLED }}", "${telegramNotification['enabled']}")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_CHAT_ID }}", "${telegramNotification['chatId']}")
        pipelineContent = pipelineContent.replace("{{ TELEGRAM_BOT_TOKEN }}", "${telegramNotification['telegramBotToken']}")

        definition {
            cps {
                script(pipelineContent)
                sandbox()
            }
        }

        logRotator {
            numToKeep(30)
        }

        if (environment == 'staging' || environment == 'prod') {
            authorization {
                for (allowedUser in canDeployProdUsers) {
                    permission("hudson.model.Item.Read:${allowedUser}")
                    permission("hudson.model.Item.Build:${allowedUser}")
                    permission("hudson.model.Item.Cancel:${allowedUser}")
                }
                blocksInheritance()
            }
        }
    }
}

services.each { service ->
    println "Creating job for: " + service.name

    def repoFolder = createFolder(service)

    if (service.need_build_job) {
        createBuildJob(project, repoFolder, service, ecrUrl, telegramNotification, kanikoVersion)
    }
//    createBuildJobSpecificBranch(project, repoFolder, service, "production")

    environments.each { environment ->
        createDeployJob(project, repoFolder, service, environment, ecrUrl, awsAccountsId[environment], awsAccountsRegion[environment], telegramNotification, canDeployProdUsers)
    }
}
