#ifndef __EM_CC_DECODER_H__
#define __EM_CC_DECODER_H__

#include "abstract_decoder.h"
#include "em_query.h"
#include "words_util.h"
#include <set>
#include <string>

#define FRIEND_TEST(test_case_name, test_name) \
    friend class test_case_name##_##test_name##_Test

class EMCcDecoder : public AbstractDecoder {
    FRIEND_TEST(EMCcDecoderTest, PredictTest1);
    friend class CwcDecoder; // to allow direct access of calcLoglikeliLM function.
public:
    class CharInputKey : public InputKey {
    public:
        CharInputKey()
            : m_val(' ')
        {
        }
        CharInputKey(const char& val)
            : m_val(val)
        {
        }
        CharInputKey(const std::string& val)
            : m_val(val[0])
        {
        }
        CharInputKey(const CharInputKey& other)
            : m_val(other.m_val)
        {
        }
        virtual ~CharInputKey() {}
        CharInputKey& operator=(const CharInputKey& other)
        {
            m_val = other.m_val;
            return *this;
        }
        CharInputKey& operator=(const char& val)
        {
            m_val = val;
            return *this;
        }
        CharInputKey& operator=(const std::string& val)
        {
            m_val = val[0];
            return *this;
        }
        virtual operator char() { return m_val; }
        virtual int getClassId() const { return CHAR_INPUT_KEY; }
        virtual std::string toString() const
        {
            std::string ret(1, m_val);
            return ret;
        }

    protected:
        char m_val;
    };
    EMCcDecoder(std::string lm_name, std::string em_name, int em_order = 4, float cer = 0.02, float lambda = 1.2, bool is_verbose = true);
    virtual ~EMCcDecoder();

    virtual std::pair<std::string, std::string> parseInputSentence(std::string sentence) override;

protected:
    virtual std::set<DString> generateCandidates(InputKey& v) override;
    virtual float calcLogLikeli(std::string context, InputKey& v, DString w) override;
    virtual float calcLogLikeliLM(std::string sentence);
    virtual float calcLogLikeliEM(std::string context, InputKey& v, DString w);
    virtual void initVocab(std::string vocab_file_path);
    virtual std::unique_ptr<std::set<std::string>> genPredictCandidates(const std::string& input, const int vocaSize) override;
    virtual std::vector<std::string> getNgram(std::string context, std::string input, std::string c) override;

    EmQuery* m_em_query_;
    WordsUtil m_words_util_;
    float m_lambda_;
    float m_cer_;
    std::set<std::string> m_vocab;

private:
};

#endif
