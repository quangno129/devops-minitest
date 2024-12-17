def call(String branchName = 'main') {
    node {
        stage("Checkout") {
            withCredentials([string(credentialsId: 'github-token', variable: 'TOKEN')]) {
                deleteDir()
                sh """
                    printenv
                    git clone -b $branchName https://$TOKEN@github.com/*******/jenkins-lib lib --depth 1
                """
            }
        }

        stage("Create folders and jobs") {
            jobDsl targets: [
                    'lib/jobs/core/*.groovy',
                    'lib/jobs/v99/*.groovy',
                    'lib/jobs/vchain/*.groovy',
            ].join('\n')
        }
    }
}
