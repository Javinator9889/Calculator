#include <jni.h>
#include "exprtk/exprtk.hpp"


double evaluate(const std::string *input) {
    typedef exprtk::symbol_table<double> symbol_table_t;
    typedef exprtk::expression<double> expression_t;
    typedef exprtk::parser<double> parser_t;

    symbol_table_t symbol_table;
    symbol_table.add_constants();

    expression_t expression;
    expression.register_symbol_table(symbol_table);

    parser_t parser;
    parser.compile(*input, expression);

    return expression.value();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_javinator9889_calculator_utils_Calculator_evaluate(JNIEnv *env,
                                                            jobject _,
                                                            jstring input) {
    jboolean isCopy;
    const char *convertedValue = env->GetStringUTFChars(input, &isCopy);
    const int length = env->GetStringLength(input);
    std::string convertedInput = std::string(convertedValue, length);
    return evaluate(&convertedInput);
}