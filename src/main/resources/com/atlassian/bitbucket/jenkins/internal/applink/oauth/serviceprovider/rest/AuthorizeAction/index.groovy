package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest.AuthorizeAction;

def c = namespace(lib.CredentialsTagLib)
def f = namespace(lib.FormTagLib)
def l = namespace(lib.LayoutTagLib)

l.layout {
    l.main_panel() {
        h2(_("Authorize Bitbucket"))
        p(_("TODO: Insert copy"))
        f.form(action: "performSubmit", method: "POST", name: _("performSubmit")) {
            f.invisibleEntry(field: "token") {
                f.readOnlyTextbox(type: "hidden", name: "id", value: "${instance.token}")
            }
            f.block() {
                f.bottomButtonBar() {
                    f.submit(name: "authorize", value: _("Authorize"))
                    f.submit(name: "cancel", value: _("Cancel"))
                }
            }
        }
    }
}