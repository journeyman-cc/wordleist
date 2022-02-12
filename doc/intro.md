# Introduction to wordleist

Wordleist is a toy system to investigate the wordle puzzle, and the questions about frequencies and distributions in English words which it reveals.

## State of play at [20220129](https://github.com/journeyman-cc/wordleist/commit/eec9f0615d692843f16f9a347daf3db51f94f99f)

Performance is surprisingly good despite an extremely naive generate algorithm. Wordlist currently solves 97.2% of all puzzles.

## Things which could definitely be improved

## The 'lazed' issue

Consider this failed run:

```clojure
{:success false, 
    :word "Failed: no more turns :-(", 
    :attempts 7, 
    :patterns (
        {:pattern ([:not-present \s] [:present \e] [:present \a] [:not-present \o] [:not-present \r]), :cands 1042} 
        {:pattern ([:not-present \w] [:not-present \h] [:not-present \i] [:not-present \c] [:not-present \h]), :cands 376} 
        {:pattern ([:not-present \t] [:found \a] [:not-present \b] [:present \l] [:present \e]), :cands 71} 
        {:pattern ([:not-present \n] [:found \a] [:not-present \m] [:found \e] [:found \d]), :cands 15} 
        {:pattern ([:not-present \f] [:found \a] [:present \d] [:found \e] [:found \d]), :cands 9} 
        {:pattern ([:not-present \g] [:found \a] [:found \z] [:found \e] [:found \d]), :cands 2} 
        {:pattern ([:present \d] [:found \a] [:found \z] [:found \e] [:found \d]), :cands 1})}
```

The character 'l' is determined to be present in pattern 3, but in patterns 5 and 6 'g' and 'd' respectively are tried. Because we have four 'found' characters and one 'present' which is not one of those 'found', the remaining character can only be 'l'.

A similar failure happens with 'fiver' despite 'f' being found in pattern 4.
