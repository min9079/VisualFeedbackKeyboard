#ifndef __CHAR_PREDICTOR_H__
#define __CHAR_PREDICTOR_H__

#include "lm_query.h"
#include <iostream>
#include <memory>
#include <set>
#include <string>

#define FRIEND_TEST(test_case_name, test_name) \
    friend class test_case_name##_##test_name##_Test

class CharPredictor {
public:
    CharPredictor(std::string lm_name, bool is_verbose = true);
    ~CharPredictor();
    bool isVerbose() { return m_is_verbose_; }
    void setVerbose(bool is_verbose) { m_is_verbose_ = is_verbose; }
    std::vector<std::string> predict(const std::string& context, const std::string& input);

    const unsigned int MAX_N_OF_RESULT = 5;

protected:
    std::unique_ptr<std::set<char>> generateCandidates();
    void initVocab(std::string vocab_file_path);
    std::vector<std::string> getNgramChars(const std::string& query_string);

    LmQuery* m_lm_query_;
    bool m_is_verbose_;
    std::unique_ptr<std::set<char>> mp_vocab;

private:
    FRIEND_TEST(CharPredictorTest, Test2);
};

#endif
