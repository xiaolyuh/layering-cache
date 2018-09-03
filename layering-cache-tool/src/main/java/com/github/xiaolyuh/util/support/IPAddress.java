package com.github.xiaolyuh.util.support;

import java.util.NoSuchElementException;

/**
 * @author yuhao.wang3
 */
public class IPAddress implements Cloneable {

    /**
     * IP 地址
     */
    protected int ipAddress = 0;

    public IPAddress(String ipAddressStr) {
        ipAddress = parseIPAddress(ipAddressStr);
    }

    public IPAddress(int address) {
        ipAddress = address;
    }


    /**
     * 返回IP地址的整数表示形式。
     *
     * @return 返回IP地址的整数表示形式
     */
    public final int getIPAddress() {
        return ipAddress;
    }

    /**
     * 返回IP地址的字符串表示形式 xxx.xxx.xxx.xxx。
     *
     * @return 返回IP地址的字符串表示形式。
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        int temp;

        temp = ipAddress & 0x000000FF;
        result.append(temp);
        result.append(".");

        temp = (ipAddress >> 8) & 0x000000FF;
        result.append(temp);
        result.append(".");

        temp = (ipAddress >> 16) & 0x000000FF;
        result.append(temp);
        result.append(".");

        temp = (ipAddress >> 24) & 0x000000FF;
        result.append(temp);

        return result.toString();
    }

    /**
     * 检查IP地址是否属于A类IP地址。
     *
     * @return 如果IP地址属于A类IP地址，则返回true，否则返回false。
     */
    public final boolean isClassA() {
        return (ipAddress & 0x00000001) == 0;
    }

    /**
     * 检查IP地址是否属于B类IP地址。
     *
     * @return 如果IP地址属于B类IP地址，则返回true，否则返回false。
     */
    public final boolean isClassB() {
        return (ipAddress & 0x00000003) == 1;
    }

    /**
     * 检查IP地址是否属于C类IP地址。
     *
     * @return 如果IP地址属于C类IP地址，则返回true，否则返回false。
     */
    public final boolean isClassC() {
        return (ipAddress & 0x00000007) == 3;
    }


    /**
     * 将IP地址的带小数点的表示法转换为32位整数值。
     *
     * @param ipAddressStr IP地址 (xxx.xxx.xxx.xxx)
     * @return 32位整数值。
     */
    final int parseIPAddress(String ipAddressStr) {
        int result = 0;

        if (ipAddressStr == null) {
            throw new IllegalArgumentException();
        }

        try {
            String tmp = ipAddressStr;

            // get the 3 first numbers
            int offset = 0;
            for (int i = 0; i < 3; i++) {

                // get the position of the first dot
                int index = tmp.indexOf('.');

                // if there is not a dot then the ip string representation is
                // not compliant to the decimal-dotted notation.
                if (index != -1) {

                    // get the number before the dot and convert it into
                    // an integer.
                    String numberStr = tmp.substring(0, index);
                    int number = Integer.parseInt(numberStr);
                    if ((number < 0) || (number > 255)) {
                        throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
                    }

                    result += number << offset;
                    offset += 8;
                    tmp = tmp.substring(index + 1);
                } else {
                    throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
                }
            }

            // the remaining part of the string should be the last number.
            if (tmp.length() > 0) {
                int number = Integer.parseInt(tmp);
                if ((number < 0) || (number > 255)) {
                    throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
                }

                result += number << offset;
                ipAddress = result;
            } else {
                throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
            }
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]", ex);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]", ex);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return this.ipAddress;
    }

    @Override
    public boolean equals(Object another) {
        return another instanceof IPAddress && ipAddress == ((IPAddress) another).ipAddress;
    }
}
