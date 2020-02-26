package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest.AuthorizeAction;

def f = namespace(lib.FormTagLib)
def l = namespace(lib.LayoutTagLib)

l.layout {
    l.main_panel() {
        h2(_("Authorize Bitbucket"))
        p(_("TODO: Insert copy"))
        f.form(action: _("performAuthorize"), method: _("submit"), name: _("performAuthorize")) {
            f.bottomButtonBar() {
                f.submit(name: _("authorize"), value: _("Authorize"))
                f.submit(name: _("cancel"), value: _("Cancel"))
            }
        }
    }
}