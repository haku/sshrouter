package com.vaguehope.sshrouter.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vaguehope.sshrouter.Main;

public class IpServlet extends HttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(IpServlet.class);
	private static final long serialVersionUID = -2826730502711068672L;

	private final AmazonEC2 ec2;
	private final LoadingCache<List<Filter>, DescribeInstancesResult> desInstCache;

	public IpServlet () throws IOException {
		final ClientConfiguration clientConfiguration = new ClientConfiguration();
		Main.findProxy(clientConfiguration);
		this.ec2 = new AmazonEC2Client(clientConfiguration);
		this.ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
		this.desInstCache = CacheBuilder.newBuilder()
	            .expireAfterWrite(5, TimeUnit.MINUTES)
	            .build(new DescribeInstances(this.ec2));
	}

	@Override
	protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final List<Filter> filters = new ArrayList<Filter>();
		for (Entry<String, String[]> param : req.getParameterMap().entrySet()) {
			filters.add(new Filter(param.getKey(), Arrays.asList(param.getValue())));
		}
		LOG.debug("Filters: {}", filters);

		final DescribeInstancesResult instancesResult = this.desInstCache.getUnchecked(filters); // FIXME lazy exception handling.
		if (instancesResult == DescribeInstances.NULL) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Invalid filter: " + filters);
			return;
		}

		final List<Instance> instances = allInstances(instancesResult);
		if (instances.size() == 1) {
			final Instance inst = instances.get(0); // FIXME when more than one match?
			final InstanceNetworkInterface nic = inst.getNetworkInterfaces().get(0); // FIXME when more than one match?
			final String ip = nic.getPrivateIpAddress();
			LOG.info("{} --> {}", filters, ip);
			resp.getWriter().print(ip);
		}
		else if (instances.size() > 1) {
			LOG.info("Multiple matches {}: {}", filters, instances);
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Multiple matches: " + filters + ": " + instances);
		}
		else {
			LOG.info("No matches {}: {}", filters, instances);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			resp.getWriter().println("No matches: " + filters);
		}
	}

	private static class DescribeInstances extends CacheLoader<List<Filter>, DescribeInstancesResult> {

	    public static final DescribeInstancesResult NULL = new DescribeInstancesResult();

        private final AmazonEC2 ec2;

        public DescribeInstances(final AmazonEC2 ec2) {
            this.ec2 = ec2;
        }

        @Override
        public DescribeInstancesResult load(final List<Filter> filters) throws Exception {
            try {
                return this.ec2.describeInstances(new DescribeInstancesRequest().withFilters(filters));
            }
            catch (final AmazonServiceException e) {
                LOG.warn("Invalid filter: {}", filters);
                return NULL;
            }
        }

	}

	private static List<Instance> allInstances (final DescribeInstancesResult instances) {
		List<Instance> insts = new ArrayList<Instance>();
		for (Reservation reser : instances.getReservations()) {
			for (Instance inst : reser.getInstances()) {
				insts.add(inst);
			}
		}
		return insts;
	}

}
