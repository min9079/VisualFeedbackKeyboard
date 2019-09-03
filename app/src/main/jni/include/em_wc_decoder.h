#ifndef __EM_WC_DECODER_H__
#define __EM_WC_DECODER_H__

#include "abstract_decoder.h"
#include "em_query.h"
#include "words_util.h"
#include <set>
#include <string>
#include <utility>

#define FRIEND_TEST(test_case_name, test_name) \
    friend class test_case_name##_##test_name##_Test

class EMWcDecoder : public AbstractDecoder {
    FRIEND_TEST(EMWcDecoderTest, PredictTest1);

public:
    class InputWord : public InputKey {
    public:
        InputWord()
            : m_val("")
        {
        }
        InputWord(const std::string& val)
            : m_val(val)
        {
        }
        InputWord(const InputWord& other)
            : m_val(other.m_val)
        {
        }
        virtual ~InputWord() {}
        InputWord& operator=(const InputWord& other)
        {
            m_val = other.m_val;
            return *this;
        }
        InputWord& operator=(const std::string& val)
        {
            m_val = val;
            return *this;
        }
        operator std::string() { return toString(); }
        virtual int getClassId() const { return INPUT_WORD; }
        virtual std::string toString() const
        {
            return m_val;
        }

    protected:
        std::string m_val;
    };

    EMWcDecoder(std::string lm_name, std::string em_name, int em_order = 4, float cer = 0.02, float lambda = 1.2, bool is_verbose = true);
    virtual ~EMWcDecoder();

    virtual std::pair<std::string, std::string> parseInputSentence(std::string sentence) override;

protected:
    virtual std::set<DString> generateCandidates(InputKey& v_word) override;
    virtual float calcLogLikeli(std::string context, InputKey& v_word, DString w_word) override;
    virtual float calcLogLikeliLM(std::string sentence);
    virtual float calcLogLikeliEM(std::string context, InputKey& v_word, DString w_word);
    virtual void initVocab(std::string vocab_file_path);
    virtual std::unique_ptr<std::set<std::string>> genPredictCandidates(const std::string& input, const int vocSize) override;
    virtual std::vector<std::string> getNgram(std::string context, std::string input, std::string c) override;

    EmQuery* m_em_query_;
    WordsUtil m_words_util_;
    float m_lambda_;
    Trie m_vocab;

private:
};
#endif
