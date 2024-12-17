package utils

class Constants {
    static final PROJECT = "Buddy_trading"


    static final NODEJS_BACKEND = [
            [
                    name             : "buddy_Trading",
                    image_name       : "buddy_Trading",
                    repo_url         : "*********************",
                    repo_name        : "buddy_Trading",
                    helmchart_oci    : ""*********************",",
                    helmchart_version: "1.0.14",
                    need_build_job   : true  // need to generate build job for this service?
            ]
    ]

    static final ENVIRONMENTS = ["dev", "qa", "prod", "sandbox", "loadtest"]

    static final ECR_URL = "*************************"

    static final S3_ARTIFACTS_BUCKET = "*************************"

    static final AWS_ACCOUNTS_ID = [
            dev     : '*************************',
            qa      : '*************************',
            prod    : '*************************',
            sandbox : '*************************',
            loadtest: '*************************'
    ]

    static final AWS_ACCOUNTS_REGION = [
            dev     : "ap-southeast-1",
            qa      : "ap-southeast-1",
            prod    : "ap-southeast-1",
            sandbox : 'ap-southeast-1',
            loadtest: 'ap-southeast-1'
    ]

    static final TELEGRAM_NOTIFICATION = [
            enabled         : true,
            chatId          : "-'*************************'",
            telegramBotToken: "'*************************"
    ]

    static final CAN_DEPLOY_PROD_USERS = [
            "'*************************'",
    ]

    static final KANIKO_VERSION = "v1.23.0-debug"

    static final AWS_CLI_VERSION = "2.15.3"

    static final MIGRATE_VERSION = "4"
}
