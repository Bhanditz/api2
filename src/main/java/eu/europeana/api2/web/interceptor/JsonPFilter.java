package eu.europeana.api2.web.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import eu.europeana.api2.utils.GenericResponseWrapper;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class JsonPFilter implements Filter {
	
	private static String CB_PARAM = "callback";

	@Override
	public void doFilter(ServletRequest sReq, ServletResponse sRes, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) sReq;
		HttpServletResponse response = (HttpServletResponse) sRes;

		@SuppressWarnings("unchecked")
		Map<String, String[]> parms = request.getParameterMap();

		if ((parms != null) && parms.containsKey(CB_PARAM) && StringUtils.isNotBlank(parms.get(CB_PARAM)[0])) {
			OutputStream out = response.getOutputStream();
			GenericResponseWrapper wrapper = new GenericResponseWrapper(response);
			chain.doFilter(request, wrapper);
			out.write(parms.get(CB_PARAM)[0].getBytes());
			out.write("(".getBytes());
			out.write(wrapper.getData());
			out.write(");".getBytes());
			out.close();
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// do nothing
	}

}
