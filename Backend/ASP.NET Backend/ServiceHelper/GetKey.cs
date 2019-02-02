using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace TEST_magiword_dev_preview.ServiceHelper
{
    public static class GetKey
    {
        public static int GetKeyByWord(string word)
        {
            if (word == null)
                return int.MinValue;
            int key = 0;
            foreach (char item in word)
            {
                key += 348485251 - ((byte)item) ^ 348485251;
            }
            return key;
        }
    }
}