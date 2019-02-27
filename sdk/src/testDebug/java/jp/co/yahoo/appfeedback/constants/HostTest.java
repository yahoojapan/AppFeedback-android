package jp.co.yahoo.appfeedback.constants;

import android.content.Context;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import jp.co.yahoo.appfeedback.R;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * Created by taicsuzu on 2017/04/26.
 */
public class HostTest {

    private Context mockContext() {
        Context mockContext = PowerMockito.mock(Context.class);
        doReturn("dev-%d").when(mockContext).getString(R.string.appfeedback_api_dev);
        doReturn("alpha").when(mockContext).getString(R.string.appfeedback_api_alpha);
        doReturn("internal").when(mockContext).getString(R.string.appfeedback_api_internal);
        doReturn("external").when(mockContext).getString(R.string.appfeedback_api_external);
        return mockContext;
    }

    @Test
    public void set() throws Exception {
        Host.set(Host.ENV.ALPHA);
        assertEquals(Host.HOST(mockContext()), "alpha");
    }

    @Test
    public void setDevURL() throws Exception {
        Host.set(Host.ENV.DEV);
        Host.setDevInstanceNum(0);
        assertEquals(Host.HOST(mockContext()), "dev-0");
    }

    @Test
    public void HOST() throws Exception {
        Host.set(Host.ENV.INTERNAL);
        assertEquals(Host.HOST(mockContext()), "internal");
    }
}
