package com.linkedin.datastream.connectors.kafka;

import java.util.Comparator;
import java.util.Objects;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;


public class KafkaBrokerAddress {
  public static final Comparator<KafkaBrokerAddress> BY_URL = (o1, o2) -> {
    int nameComparison = o1._hostName.compareTo(o2._hostName);
    if (nameComparison != 0) {
      return nameComparison;
    }
    return Integer.compare(o1._portNumber, o2._portNumber);
  };

  private final String _hostName;
  private final int _portNumber;

  public KafkaBrokerAddress(String hostName, int portNumber) {
    validatePortNumber(portNumber);
    this._hostName = validateHostname(hostName);
    this._portNumber = portNumber;
  }

  public String getHostName() {
    return _hostName;
  }

  public int getPortNumber() {
    return _portNumber;
  }

  @Override
  public String toString() {
    return _hostName + ":" + _portNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KafkaBrokerAddress that = (KafkaBrokerAddress) o;
    return _portNumber == that._portNumber && Objects.equals(_hostName, that._hostName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostName, _portNumber);
  }

  public static KafkaBrokerAddress valueOf(String brokerAddress) throws IllegalArgumentException {
    if (brokerAddress == null) {
      //noinspection ConstantConditions
      badArg(brokerAddress);
    }
    String str = brokerAddress.trim();
    if (str.isEmpty()) {
      badArg(brokerAddress);
    }
    int lastColon = str.lastIndexOf(":");
    if (lastColon == -1) {
      badArg(brokerAddress); //at least host:port
    }
    int portNumber = -1;
    try {
      portNumber = Integer.parseInt(str.substring(lastColon + 1).trim());
      validatePortNumber(portNumber);
    } catch (NumberFormatException e) {
      badArg(brokerAddress);
    }
    String hostName = str.substring(0, lastColon);
    hostName = validateHostname(hostName);

    return new KafkaBrokerAddress(hostName, portNumber);
  }

  private static void validatePortNumber(int portNumber) throws IllegalArgumentException {
    if (portNumber <= 0 || portNumber > 65535) {
      throw new IllegalArgumentException(portNumber + " is not a valid port number");
    }
  }

  private static String validateHostname(String hostName) throws IllegalArgumentException {
    if (hostName == null) {
      throw new IllegalArgumentException("null host name");
    }
    String trimmed = hostName.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("empty host name");
    }
    //we allow ipv4/6 addresses and RFC 1123 host names
    InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
    DomainValidator domainValidator = DomainValidator.getInstance(true);
    boolean valid =
        domainValidator.isValid(trimmed)
            || inetAddressValidator.isValidInet4Address(trimmed)
            || inetAddressValidator.isValidInet6Address(trimmed);
    if (!valid) {
      throw new IllegalArgumentException(trimmed + " is not a valid hostname or ip");
    }
    return trimmed;
  }

  private static void badArg(String arg) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.valueOf(arg) + " is not a valid broker address");
  }
}