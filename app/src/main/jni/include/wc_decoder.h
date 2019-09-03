#ifndef __WC_DECODER_H__
#define __WC_DECODER_H__

#include "em_query.h"
#include "lm_query.h"
#include "words_util.h"
#include <string>

class WcDecoder {
public:
    WcDecoder(std::string lm_name, std::string em_name, int em_order = 4, float ncer = 0.02, bool show_words = true);
    virtual ~WcDecoder();
    bool isShowWords();
    void setShowWords(bool show_words);
    std::vector<std::pair<std::string, float>> decode(std::string sentence, int k = 5);
    bool isKnown(std::string word);

private:
    bool m_show_words_;
    LmQuery* m_lm_query_;
    EmQuery* m_em_query_;
    WordsUtil m_words_util_;
    template <bool isFirst, bool isInverse>
    static bool pairCompare_(const std::pair<std::string, float>& a, const std::pair<std::string, float>& b);
};
#endif
