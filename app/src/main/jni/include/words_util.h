#ifndef __WORD_UTILS_H__
#define __WORD_UTILS_H__
#include <set>
#include <string>
#include <utility>
#include <vector>

class WordsUtil {
public:
    static const unsigned int MaxWordLen = 64;
    WordsUtil()
    {
    }

    std::set<std::string> wordsEditByDistance(std::string word, int distance);
    int getLDistance(std::string w, std::string v, std::vector<std::pair<std::string, std::string>>* operations);
    std::vector<std::string> tokenizeSentence(std::string sentence);

private:
    unsigned int m_table_[MaxWordLen + 1][MaxWordLen + 1];
    std::set<std::string> wordsEditByDistance1_(std::string word);
    void getLDistanceTable_(std::string w, std::string v);
    void getOperations_(std::string w1, std::string w2, std::vector<std::pair<std::string, std::string>>* operations);
};

class TrieNode;

class Trie {
public:
    Trie();
    ~Trie();
    void addWord(std::string word);
    void addWord(std::string word, int rank);
    bool searchWord(std::string word);
    bool searchWord(std::string word, int rank);
    int getWords(std::set<std::string>& word_set, std::string prefix);
    int getWords(std::set<std::string>& word_set, std::string prefix, int rank);
    int getWords(std::vector<std::string>& words, std::string prefix, int rank);
    int size() { return mSize; }

private:
    TrieNode* mRoot = NULL;
    int mSize = 0;
    int mRank = 0;
};

#endif
