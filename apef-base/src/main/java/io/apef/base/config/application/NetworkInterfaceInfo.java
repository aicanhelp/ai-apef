package io.apef.base.config.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class NetworkInterfaceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private boolean up;
    private String title;
    private String[] hostAddresses;
    private String hardwareAddress;

    public NetworkInterfaceInfo(final NetworkInterface networkInterface) {
        super();
        setNetworkInterface(networkInterface);
    }

    private NetworkInterfaceInfo setNetworkInterface(final NetworkInterface networkInterface) {
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }

        hostAddresses = getHostAddresses(networkInterface);
        name = networkInterface.getName();
        title = networkInterface.getDisplayName();
        try {
            up = networkInterface.isUp();
            hardwareAddress = buildReadableHardwareAddress(networkInterface);
        } catch (final SocketException cause) {
            log.warn("Unexpected error getting status from {}.", title, cause);
        }
        return this;
    }

    private String buildReadableHardwareAddress(final NetworkInterface networkInterface) throws SocketException {
        String readableHardwareAddress;
        final byte[] bytes = networkInterface.getHardwareAddress();
        if (bytes == null) {
            readableHardwareAddress = null;
        } else {
            final StringBuilder buffer = new StringBuilder();
            final int nbytes = bytes.length;
            for (int ibyte = 0; ibyte < nbytes; ibyte += 1) {
                if (ibyte > 0) {
                    buffer.append('-');
                }
                final byte b = bytes[ibyte];
                appendHexDigit((b >> 4) & 15, buffer);
                appendHexDigit((b & 15), buffer);
            }
            readableHardwareAddress = buffer.toString();
        }
        return readableHardwareAddress;
    }

    private void appendHexDigit(int nibble, final StringBuilder buffer) {
        if (nibble < 10) {
            buffer.append((char) (nibble + '0'));
        } else {
            buffer.append((char) (nibble + 'A' - 10));
        }
    }

    private String[] getHostAddresses(final NetworkInterface networkInterface) {
        // Get the host addresses for the network interface
        // and sort them.
        final List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
        final int ninetAddresses = inetAddresses.size();
        final List<String> hostAddresses = new ArrayList<>(ninetAddresses);
        for (InetAddress inetAddress : inetAddresses) {
            final String hostAddress = inetAddress.getHostAddress();
            hostAddresses.add(hostAddress);
        }

        Collections.sort(hostAddresses);

        final String[] result = new String[ninetAddresses];
        hostAddresses.toArray(result);
        return result;
    }

    public static List<NetworkInterfaceInfo> NetworkInterfaces() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces())
                    .stream().map(NetworkInterfaceInfo::new
                    ).collect(Collectors.toList());
        } catch (final SocketException cause) {
            log.warn("Unexpected error getting network interface information.", cause);
        }
        return null;
    }
}
