#ifndef __ABSTRACT_DECODER_H__
#define __ABSTRACT_DECODER_H__

#include "lm_query.h"
#include <iostream>
#include <memory> // for shared_ptr c++11
#include <mutex>
#include <set>
#include <string>
#include <utility>

enum { INPUT_KEY,
    CHAR_INPUT_KEY,
    TOUCH_INPUT_KEY,
    INPUT_WORD,
    TOUCH_INPUT_WORD,
    INPUT_SENTENCE };

class AbstractDecoder {
public:
    class InputKey {
    public:
        InputKey()
            : m_is_valid(true)
        {
        }
        virtual ~InputKey() {}
        virtual int getClassId() const { return INPUT_KEY; }
        void setInvalid() { m_is_valid = false; }
        void setValid() { m_is_valid = true; }
        bool isValid() { return m_is_valid; }
        friend std::ostream& operator<<(std::ostream& os, const InputKey& key);
        virtual std::string toString() const { return ""; };

    protected:
        bool m_is_valid;
    };

    class DString {
    public:
        DString() {}
        DString(char in_char, InputKey& key)
        {
            std::shared_ptr<InputKey> p_key(new InputKey(key));
            m_keys.push_back(std::make_pair(in_char, p_key));
        }
        DString(std::string word)
        {
            std::shared_ptr<InputKey> p_key(new InputKey());
            for (int i = 0; i < word.size(); i++) {
                m_keys.push_back(std::make_pair(word[i], p_key));
            }
        }
        ~DString() { clear(); }

        std::pair<char, std::shared_ptr<AbstractDecoder::InputKey>>& operator[](std::size_t idx);
        void push_back(std::pair<char, std::shared_ptr<AbstractDecoder::InputKey>> other);
        void push_back_(char in_key, std::shared_ptr<AbstractDecoder::InputKey> p_key);
        void pop_back() { m_keys.pop_back(); }
        int size() { return m_keys.size(); }
        void clear();
        void appendStr(DString& rhs);
        std::string toString() const;
        int compare(const DString& my_str) const;
        friend bool operator<(const DString& lsh, const DString& rsh)
        {
            if (lsh.compare(rsh) < 0)
                return true;
            else
                return false;
        }
        friend std::ostream& operator<<(std::ostream& os, const DString& myString);

    private:
        std::vector<std::pair<char, std::shared_ptr<AbstractDecoder::InputKey>>> m_keys;
        // TODO: consider to block copy constructor and copy operator.
        // or seperating std:string and InputKey to speed up.
    };

    AbstractDecoder(std::string lm_name, bool is_verbose = true);
    virtual ~AbstractDecoder();
    bool isVerbose();
    void setVerbose(bool is_verbose);
    std::vector<std::pair<DString, float>> decode(std::string context, InputKey& v, int k = 5);
    virtual std::pair<std::string, std::string> parseInputSentence(std::string sentence) = 0;
    std::vector<std::pair<std::string, float>> predict(const std::string& context, const std::string& input, const int vocaSize = 1024);

protected:
    virtual std::set<DString> generateCandidates(InputKey& v) = 0;
    virtual float calcLogLikeli(std::string context, InputKey& v, DString w) = 0;
    template <bool isInverse>
    static bool pairCompare_(const std::pair<AbstractDecoder::DString, float>& a, const std::pair<AbstractDecoder::DString, float>& b);
    virtual std::unique_ptr<std::set<std::string>> genPredictCandidates(const std::string& input, const int wocaSize) = 0;
    virtual std::vector<std::string> getNgram(std::string context, std::string input, std::string c) = 0;

    LmQuery* m_lm_query_;
    bool m_is_verbose_;
    std::mutex m_mutex;

private:
};

#endif
