# Chit: size-oriented R8 rules.
-allowaccessmodification
-repackageclasses ''
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
