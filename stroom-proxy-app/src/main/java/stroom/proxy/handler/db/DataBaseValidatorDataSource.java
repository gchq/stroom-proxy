package stroom.proxy.handler.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.StroomStatusCode;
import stroom.proxy.handler.StroomStreamException;

public class DataBaseValidatorDataSource {
    private final static StroomLogger LOGGER = StroomLogger.getLogger(DataBaseValidatorDataSource.class);

    public final static String SQL_Y = "Y";
    public final static String SQL_N = "N";
    public final static String SQL_RECEIVE = "RECEIVE";
    public final static String SQL_REQ_EFFECTIVE = "REQ_EFFECTIVE";
    public final static String SQL_REQ_CERT = "REQ_CERT";
    public final static String SQL_CHECK_CERT = "CHECK_CERT";

    private String dbRequestValidatorContext;
    private String dbRequestValidatorJndiName;
    private Context context;

    private Context getContext() throws NamingException {
        if (context == null) {
            LOGGER.debug("getContext() - %s", dbRequestValidatorContext);
            Context initCtx = new InitialContext();
            context = (Context) initCtx.lookup(dbRequestValidatorContext);
        }
        return context;
    }

    public DataSource getDataSource() {
        try {
            LOGGER.debug("getDataSource() - %s", dbRequestValidatorJndiName);
            return (DataSource) getContext().lookup(dbRequestValidatorJndiName);
        } catch (NamingException nex) {
            LOGGER.error("getDataSource()", nex);
            throw new StroomStreamException(StroomStatusCode.UNKNOWN_ERROR, nex.getMessage());
        }
    }

    public boolean isActive() {
        return StringUtils.hasText(dbRequestValidatorContext) && StringUtils.hasText(dbRequestValidatorJndiName);
    }

    public void setDbRequestValidatorContext(String dbRequestValidatorContext) {
        this.dbRequestValidatorContext = dbRequestValidatorContext;
    }

    public void setDbRequestValidatorJndiName(String dbRequestValidatorJndiName) {
        this.dbRequestValidatorJndiName = dbRequestValidatorJndiName;
    }

}
