// Create view for the PR check jobs
def createPRCheckView() {
    listView("PR-check") {

        filterBuildQueue()
        filterExecutors()

        jobs {
            regex(/.+-PR-check/)
        }

        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }
}

//createPRCheckView()
