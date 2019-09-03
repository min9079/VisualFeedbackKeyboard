#ifndef __LM_QUERY_H__
#define __LM_QUERY_H__

#include <string>
#include <vector>

namespace lm {
namespace ngram {
    class ProbingModel;
}
}

class LmQuery {
public:
    static const std::string LM_PATH;
    static std::string buildLmPath(std::string lm_name);

    LmQuery(std::string lm_name, bool sentence_context = false, bool show_words = true);
    virtual ~LmQuery();
    bool isSentenceContext();
    void setSentenceContext(bool sentence_context);
    bool isShowWords();
    void setShowWords(bool show_words);
    float calcLmProb(std::string ngram_words);
    float calcLmProb(std::vector<std::string> ngram_words);
    bool isKnown(std::string word);
    int getOrder();

protected:
    bool m_show_words_;

private:
    std::string m_lm_path_;
    lm::ngram::ProbingModel* m_model_;
    bool m_sentence_context_;
};
#endif
