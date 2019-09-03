#ifndef __EM_SC_DECODER_H__
#define __EM_SC_DECODER_H__

#include "abstract_decoder.h"

#define FRIEND_TEST(test_case_name, test_name) \
    friend class test_case_name##_##test_name##_Test

class EMScDecoder : public AbstractDecoder {
    FRIEND_TEST(EMScDecoderTest, Test1);

public:
    class InputSentence : public InputKey {
    public:
        InputSentence() {}
        InputSentence(const std::vector<std::vector<std::string>>& val)
            : m_words(val)
        {
        }
        InputSentence(const InputSentence& other)
            : m_words(other.m_words)
        {
        }
        virtual ~InputSentence() {}
        InputSentence& operator=(const InputSentence& other)
        {
            m_words = other.m_words;
            return *this;
        }
        InputSentence& operator=(const std::vector<std::vector<std::string>>& val)
        {
            m_words = val;
            return *this;
        }
        operator std::string() { return toString(); }
        virtual int getClassId() const { return INPUT_SENTENCE; }
        void clear();
        int pushWord(std::vector<std::string> word);
        int popWord();
        std::vector<std::vector<std::string>>& getWords() { return m_words; }
        virtual std::string toString() const
        {
            if (0 == m_words.size())
                return "";

            std::string ret_str;
            for (auto word : m_words) {
                ret_str += word[0] + " ";
            }
            return ret_str.substr(0, ret_str.length() - 1);
        }

    protected:
        std::vector<std::vector<std::string>> m_words;
    };

    EMScDecoder(std::string lm_name, bool is_verbose = true);
    virtual ~EMScDecoder();

    virtual std::pair<std::string, std::string> parseInputSentence(std::string sentence) override;

protected:
    std::vector<std::vector<std::string>> m_words;
    virtual std::set<DString> generateCandidates(InputKey& v_sentence_) override;
    virtual float calcLogLikeli(std::string context, InputKey& v, DString w) override;
    virtual std::unique_ptr<std::set<std::string>> genPredictCandidates(const std::string& input, const int vocaSize) override;
    virtual std::vector<std::string> getNgram(std::string context, std::string input, std::string c) override;
};
#endif
