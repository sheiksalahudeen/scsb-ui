package org.recap.security;

import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;

import java.net.URL;

/**
 * Created by sheiks on 25/01/17.
 */
public abstract class ReCAPAbstractCasProtocolUrlBasedTicketValidator extends ReCAPAbstractUrlBasedTicketValidator {

    protected ReCAPAbstractCasProtocolUrlBasedTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    /**
     * Retrieves the response from the server by opening a connection and merely reading the response.
     */
    protected final String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        return CommonUtils.getResponseFromServer(validationUrl, getURLConnectionFactory(), getEncoding());
    }
}

