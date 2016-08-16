/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkale.oss.base;

import java.io.IOException;
import java.util.logging.*;
import javax.annotation.Resource;
import org.redkale.convert.json.JsonConvert;
import org.redkale.net.http.*;
import org.redkale.oss.sys.UserMemberService;
import org.redkale.service.RetResult;
import org.redkale.source.Flipper;

/**
 *
 * @author zhangjx
 */
public class BaseServlet extends org.redkale.net.http.BasedHttpServlet {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    protected final boolean fine = logger.isLoggable(Level.FINE);

    protected final boolean finer = logger.isLoggable(Level.FINER);

    protected final boolean finest = logger.isLoggable(Level.FINEST);

    protected static final RetResult RET_UNLOGIN = OssRetCodes.retResult(OssRetCodes.RET_USER_UNLOGIN);

    protected static final RetResult RET_AUTHILLEGAL = OssRetCodes.retResult(OssRetCodes.RET_USER_AUTH_ILLEGAL);

    @Resource
    protected JsonConvert convert;

    @Resource
    private UserMemberService service;

    @Override
    public boolean authenticate(int module, int actionid, HttpRequest request, HttpResponse response) throws IOException {
        MemberInfo info = currentMember(request);
        if (info == null) {
            response.finishJson(RET_UNLOGIN);
            return false;
        } else if (!info.checkAuth(module, actionid)) {
            response.finishJson(RET_AUTHILLEGAL);
            return false;
        }
        return true;
    }

    protected final MemberInfo currentMember(HttpRequest req) throws IOException {
        final String sessionid = req.getSessionid(false);
        if (sessionid == null) return null;
        MemberInfo user = (MemberInfo) req.getAttribute("$_CURRENT_MEMBER");
        if (user == null) {
            user = service.current(sessionid);
            req.setAttribute("$_CURRENT_MEMBER", user);
        }
        return user;
    }

    protected Flipper findFlipper(HttpRequest request) {  //bootstrap datatable
        return findFlipper(request, Flipper.DEFAULT_LIMIT);
    }

    protected Flipper findFlipper(HttpRequest request, int defaultLimit) {  //bootstrap datatable
        int pageSize = request.getIntParameter("length", defaultLimit > 0 ? defaultLimit : Flipper.DEFAULT_LIMIT);
        if (pageSize < 1) pageSize = defaultLimit > 0 ? defaultLimit : Flipper.DEFAULT_LIMIT;
        int offset = request.getIntParameter("start", 0);
        String sort = request.getParameter("sort");
        String order = request.getParameter("order");
        String sortColumn = (sort == null ? "" : ((order == null ? sort : (sort + " " + order.toUpperCase()))));
        return new Flipper(pageSize, offset, sortColumn);
    }

}
