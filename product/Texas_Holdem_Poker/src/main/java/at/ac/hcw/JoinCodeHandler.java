package at.ac.hcw;

public class JoinCodeHandler {
    public static String joinCodeToIPv4(String joinCode) {
        if (joinCode.length() != 8) {
            throw new IllegalArgumentException("JoinCode muss genau 8 Zeichen haben");
        }
        StringBuilder IPv4 = new StringBuilder();
        for (int i = 0; i < 8; i += 2) {
            char firstChar = joinCode.charAt(i);
            char secondChar = joinCode.charAt(i + 1);
            int firstNumber = firstChar - 'A';  // zurück zur Zahl
            int secondNumber = secondChar - 'A';
            int octet = firstNumber * 26 + secondNumber; // Oktett berechnen
            IPv4.append(octet);
            if (i < 6) { // Punkte zwischen Oktetten
                IPv4.append(".");
            }
        }
        //System.out.println(IPv4.toString());
        return IPv4.toString();
    }

    public static String IPv4ToJoinCode(String IPv4) {
        String[] octets = IPv4.split("\\.");
        StringBuilder joinCode = new StringBuilder();
        for (String octet : octets) {
            int firstCharNumber = Integer.parseInt(octet) / 26;
            int secondCharNumber = Integer.parseInt(octet) % 26;
            char firstChar = (char) ('A' + firstCharNumber);
            char secondChar = (char) ('A' + secondCharNumber);
            joinCode.append(firstChar).append(secondChar);
        }
//        System.out.println(joinCode.toString()); //dominik desktop pc code HKGMAAHS
        return joinCode.toString();
    }
}
