package com.atlassian.bitbucket.jenkins.internal.config.BitbucketTokenCredentialsImpl

def f = namespace(lib.FormTagLib)

f.entry(title: _("bitbucket.admin.token"), field: "secret") {
    f.password(value: instance?.secret)
}

f.entry(title: _("bitbucket.admin.token.description"), field: "description") {
    f.textbox()
}